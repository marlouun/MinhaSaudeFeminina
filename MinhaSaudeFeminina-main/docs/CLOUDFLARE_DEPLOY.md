# Deploy do painel no Cloudflare Workers

O painel administrativo fica em `admin-web` e é compilado pelo Vite para `admin-web/dist`.

O arquivo `wrangler.jsonc` na raiz já está configurado para publicar essa pasta como Static Assets e tratar as rotas do React como SPA.

## Configuração na tela do Cloudflare

Ao conectar o repositório `marlouun/MinhaSaudeFeminina`, use:

```text
Project name: minhasaudeadmin
Production branch: main
Build command: cd admin-web && npm install && npm run build
Deploy command: npx wrangler deploy
```

Não altere o diretório de assets manualmente: o Wrangler lê `./admin-web/dist` do arquivo `wrangler.jsonc`.

## O que acontece no deploy

1. O Cloudflare clona o repositório.
2. O comando de build entra em `admin-web`, instala as dependências e executa o build Vite.
3. O Vite gera `admin-web/dist`.
4. `npx wrangler deploy` lê `wrangler.jsonc`.
5. O conteúdo de `admin-web/dist` é publicado em um endereço `workers.dev`.

## Rotas do React

A opção `not_found_handling: single-page-application` faz com que URLs como `/articles`, `/articles/new` e `/dashboard` sejam encaminhadas para o `index.html`, evitando erro 404 ao atualizar a página.

## Supabase

O painel acessa o Supabase diretamente pelo navegador usando apenas a Project URL e a Publishable Key. Nenhuma `service_role` ou chave secreta deve ser adicionada ao Cloudflare ou ao repositório.

Para o painel conseguir criar, editar e excluir artigos, o usuário administrador deve existir em Supabase Authentication e as políticas de RLS de `supabase/articles-admin-policies.sql` devem ter sido executadas.
