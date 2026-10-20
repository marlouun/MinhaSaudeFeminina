-- Test phase: no per-identity call limits; retain global caps, sessions and kill switch.
CREATE OR REPLACE FUNCTION public.reserve_chat_quota(p_user_id uuid, p_session_id uuid)
RETURNS jsonb LANGUAGE plpgsql SECURITY DEFINER SET search_path = '' AS $$
DECLARE
  v_now timestamptz := clock_timestamp();
  v_day date := (v_now at time zone 'America/Los_Angeles')::date;
  v_minute timestamptz := date_trunc('minute', v_now);
  v_actor text := coalesce(p_user_id::text, 'server');
  v_global private.chat_usage%rowtype;
BEGIN
  IF NOT coalesce((SELECT enabled FROM private.chat_settings WHERE singleton), false) THEN
    RETURN jsonb_build_object('status', 'disabled');
  END IF;
  IF p_user_id IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM auth.users u
    JOIN auth.sessions s ON s.user_id = u.id
    WHERE u.id = p_user_id AND s.id = p_session_id
      AND (u.banned_until IS NULL OR u.banned_until < v_now)
      AND (s.not_after IS NULL OR s.not_after > v_now)
  ) THEN RETURN jsonb_build_object('status', 'forbidden'); END IF;

  PERFORM pg_advisory_xact_lock(703158029);
  DELETE FROM private.chat_usage WHERE quota_day < v_day - 2;
  INSERT INTO private.chat_usage(quota_day, actor, minute_start)
  VALUES (v_day, 'global', v_minute), (v_day, v_actor, v_minute)
  ON CONFLICT DO NOTHING;
  SELECT * INTO v_global FROM private.chat_usage WHERE quota_day = v_day AND actor = 'global';
  IF v_global.calls >= 18
     OR (v_global.minute_start = v_minute AND v_global.minute_calls >= 8) THEN
    RETURN jsonb_build_object('status', 'limited');
  END IF;
  UPDATE private.chat_usage SET calls = calls + 1,
    minute_calls = CASE WHEN minute_start = v_minute THEN minute_calls + 1 ELSE 1 END,
    minute_start = v_minute
  WHERE quota_day = v_day AND actor IN ('global', v_actor);
  RETURN jsonb_build_object('status', 'ok');
END;
$$;
REVOKE ALL ON FUNCTION public.reserve_chat_quota(uuid, uuid) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.reserve_chat_quota(uuid, uuid) TO service_role;
