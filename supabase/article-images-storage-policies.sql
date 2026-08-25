-- Bucket esperado: article-images (PUBLIC)
-- Execute este arquivo no Supabase SQL Editor.

alter table storage.objects enable row level security;

-- Leitura pública das imagens é feita pelo bucket público.
-- As policies abaixo controlam quem pode gerenciar arquivos no bucket.

drop policy if exists "Admin autenticado pode visualizar imagens de artigos" on storage.objects;
drop policy if exists "Admin autenticado pode enviar imagens de artigos" on storage.objects;
drop policy if exists "Admin autenticado pode substituir imagens de artigos" on storage.objects;
drop policy if exists "Admin autenticado pode excluir imagens de artigos" on storage.objects;

create policy "Admin autenticado pode visualizar imagens de artigos"
on storage.objects
for select
to authenticated
using (bucket_id = 'article-images');

create policy "Admin autenticado pode enviar imagens de artigos"
on storage.objects
for insert
to authenticated
with check (bucket_id = 'article-images');

create policy "Admin autenticado pode substituir imagens de artigos"
on storage.objects
for update
to authenticated
using (bucket_id = 'article-images')
with check (bucket_id = 'article-images');

create policy "Admin autenticado pode excluir imagens de artigos"
on storage.objects
for delete
to authenticated
using (bucket_id = 'article-images');
