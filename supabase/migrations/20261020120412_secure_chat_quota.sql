-- No prompts, health data, e-mails or IP addresses are stored here.
create schema if not exists private;
revoke all on schema private from public, anon, authenticated;

create table private.chat_access (
  user_id uuid primary key references auth.users(id) on delete cascade,
  enabled boolean not null default false
);
create table private.chat_settings (
  singleton boolean primary key default true check (singleton),
  enabled boolean not null default true
);
insert into private.chat_settings(singleton) values (true);
create table private.chat_usage (
  quota_day date not null,
  actor text not null,
  calls integer not null default 0 check (calls >= 0),
  minute_start timestamptz not null,
  minute_calls integer not null default 0 check (minute_calls >= 0),
  primary key (quota_day, actor)
);
alter table private.chat_access enable row level security;
alter table private.chat_settings enable row level security;
alter table private.chat_usage enable row level security;
revoke all on all tables in schema private from public, anon, authenticated;

-- Only the Edge Function's server credential can execute this operation.
-- A global transaction lock serializes reservations, including concurrent
-- calls from different users/instances. Denied requests reserve nothing.
create function public.reserve_chat_quota(p_user_id uuid, p_session_id uuid)
returns jsonb language plpgsql security definer set search_path = '' as $$
declare
  v_now timestamptz := clock_timestamp();
  v_day date := (v_now at time zone 'America/Los_Angeles')::date;
  v_minute timestamptz := date_trunc('minute', v_now);
  v_actor text := coalesce(p_user_id::text, 'server');
  v_global private.chat_usage%rowtype;
  v_user private.chat_usage%rowtype;
begin
  if not coalesce((select enabled from private.chat_settings where singleton), false) then
    return jsonb_build_object('status', 'disabled');
  end if;
  if p_user_id is not null and not exists (
    select 1 from private.chat_access a
    join auth.users u on u.id = a.user_id
    join auth.sessions s on s.user_id = u.id and s.id = p_session_id
    where a.user_id = p_user_id and a.enabled
      and u.email_confirmed_at is not null and not u.is_anonymous
      and (u.banned_until is null or u.banned_until < v_now)
      and (s.not_after is null or s.not_after > v_now)
  ) then return jsonb_build_object('status', 'forbidden'); end if;

  perform pg_advisory_xact_lock(703158029);
  delete from private.chat_usage where quota_day < v_day - 2;
  insert into private.chat_usage(quota_day, actor, minute_start)
  values (v_day, 'global', v_minute), (v_day, v_actor, v_minute)
  on conflict do nothing;
  select * into v_global from private.chat_usage where quota_day = v_day and actor = 'global';
  select * into v_user from private.chat_usage where quota_day = v_day and actor = v_actor;
  if v_global.calls >= 18 or v_user.calls >= 5
     or (v_global.minute_start = v_minute and v_global.minute_calls >= 8)
     or (v_user.minute_start = v_minute and v_user.minute_calls >= 2) then
    return jsonb_build_object('status', 'limited');
  end if;
  update private.chat_usage set calls = calls + 1,
    minute_calls = case when minute_start = v_minute then minute_calls + 1 else 1 end,
    minute_start = v_minute
  where quota_day = v_day and actor in ('global', v_actor);
  return jsonb_build_object('status', 'ok');
end;
$$;
revoke all on function public.reserve_chat_quota(uuid, uuid) from public, anon, authenticated;
grant execute on function public.reserve_chat_quota(uuid, uuid) to service_role;
