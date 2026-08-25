-- Execute este arquivo no SQL Editor do Supabase depois de criar a tabela public.articles.
-- O app Android continua anônimo e só lê artigos publicados.
-- O painel usa Supabase Auth e somente usuários autenticados podem administrar artigos.

alter table public.articles enable row level security;

grant select on table public.articles to anon;
grant select, insert, update, delete on table public.articles to authenticated;

drop policy if exists "Android pode ler artigos publicados" on public.articles;
drop policy if exists "Admin autenticado pode visualizar artigos" on public.articles;
drop policy if exists "Admin autenticado pode criar artigos" on public.articles;
drop policy if exists "Admin autenticado pode editar artigos" on public.articles;
drop policy if exists "Admin autenticado pode excluir artigos" on public.articles;

create policy "Android pode ler artigos publicados"
on public.articles
for select
to anon
using (status = 'published');

create policy "Admin autenticado pode visualizar artigos"
on public.articles
for select
to authenticated
using (true);

create policy "Admin autenticado pode criar artigos"
on public.articles
for insert
to authenticated
with check (true);

create policy "Admin autenticado pode editar artigos"
on public.articles
for update
to authenticated
using (true)
with check (true);

create policy "Admin autenticado pode excluir artigos"
on public.articles
for delete
to authenticated
using (true);
