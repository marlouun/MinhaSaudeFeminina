-- Anonymous Chat Auth: preserve the admin account and isolate article authorization.
-- Exactly one confirmed non-anonymous Supabase Auth account existed when audited.
-- New admin accounts must be granted explicitly in private.article_admins.
CREATE TABLE IF NOT EXISTS private.article_admins (
  user_id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE
);
INSERT INTO private.article_admins(user_id)
SELECT id FROM auth.users
WHERE email_confirmed_at IS NOT NULL AND NOT coalesce(is_anonymous, false)
ON CONFLICT DO NOTHING;
ALTER TABLE private.article_admins ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON private.article_admins FROM PUBLIC, anon, authenticated;

-- This schema is not exposed in the Data API. Roles can call only this function.
CREATE OR REPLACE FUNCTION private.is_article_admin()
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER SET search_path = ''
AS $$
  SELECT EXISTS (
    SELECT 1 FROM private.article_admins
    WHERE user_id = (SELECT auth.uid())
  );
$$;
REVOKE ALL ON FUNCTION private.is_article_admin() FROM PUBLIC, anon, authenticated;
GRANT USAGE ON SCHEMA private TO authenticated;
GRANT EXECUTE ON FUNCTION private.is_article_admin() TO authenticated;

DROP POLICY IF EXISTS "Admin autenticado pode visualizar artigos" ON public.articles;
DROP POLICY IF EXISTS "Admin autenticado pode criar artigos" ON public.articles;
DROP POLICY IF EXISTS "Admin autenticado pode editar artigos" ON public.articles;
DROP POLICY IF EXISTS "Admin autenticado pode excluir artigos" ON public.articles;
CREATE POLICY "Admin consulta artigos; usuarias veem publicados"
ON public.articles FOR SELECT TO authenticated
USING (status = 'published' OR (SELECT private.is_article_admin()));
CREATE POLICY "Apenas admin cria artigos"
ON public.articles FOR INSERT TO authenticated
WITH CHECK ((SELECT private.is_article_admin()));
CREATE POLICY "Apenas admin edita artigos"
ON public.articles FOR UPDATE TO authenticated
USING ((SELECT private.is_article_admin()))
WITH CHECK ((SELECT private.is_article_admin()));
CREATE POLICY "Apenas admin exclui artigos"
ON public.articles FOR DELETE TO authenticated
USING ((SELECT private.is_article_admin()));

DROP POLICY IF EXISTS "Admin autenticado pode enviar imagens de artigos" ON storage.objects;
DROP POLICY IF EXISTS "Admin autenticado pode excluir imagens de artigos" ON storage.objects;
DROP POLICY IF EXISTS "Admin autenticado pode substituir imagens de artigos" ON storage.objects;
DROP POLICY IF EXISTS "Admin autenticado pode visualizar imagens de artigos" ON storage.objects;
CREATE POLICY "Apenas admin envia imagens de artigos"
ON storage.objects FOR INSERT TO authenticated
WITH CHECK (bucket_id = 'article-images' AND (SELECT private.is_article_admin()));
CREATE POLICY "Apenas admin exclui imagens de artigos"
ON storage.objects FOR DELETE TO authenticated
USING (bucket_id = 'article-images' AND (SELECT private.is_article_admin()));
CREATE POLICY "Apenas admin substitui imagens de artigos"
ON storage.objects FOR UPDATE TO authenticated
USING (bucket_id = 'article-images' AND (SELECT private.is_article_admin()))
WITH CHECK (bucket_id = 'article-images' AND (SELECT private.is_article_admin()));
CREATE POLICY "Apenas admin lista imagens de artigos"
ON storage.objects FOR SELECT TO authenticated
USING (bucket_id = 'article-images' AND (SELECT private.is_article_admin()));

-- Keep server-validated sessions, atomic quota, the per-installation identity,
-- and a hard project-wide ceiling; remove the AI email allowlist restriction.
CREATE OR REPLACE FUNCTION public.reserve_chat_quota(p_user_id uuid, p_session_id uuid)
RETURNS jsonb LANGUAGE plpgsql SECURITY DEFINER SET search_path = '' AS $$
DECLARE
  v_now timestamptz := clock_timestamp();
  v_day date := (v_now at time zone 'America/Los_Angeles')::date;
  v_minute timestamptz := date_trunc('minute', v_now);
  v_actor text := coalesce(p_user_id::text, 'server');
  v_global private.chat_usage%rowtype;
  v_user private.chat_usage%rowtype;
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
  SELECT * INTO v_user FROM private.chat_usage WHERE quota_day = v_day AND actor = v_actor;
  IF v_global.calls >= 18 OR v_user.calls >= 5
     OR (v_global.minute_start = v_minute AND v_global.minute_calls >= 8)
     OR (v_user.minute_start = v_minute AND v_user.minute_calls >= 2) THEN
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
