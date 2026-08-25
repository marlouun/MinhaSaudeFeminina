# Painel administrativo — Minha Saúde Feminina

Aplicação web separada para criar, revisar, visualizar e publicar artigos do projeto. Esta primeira versão funciona sem servidor e salva os dados no IndexedDB do navegador por meio do Dexie.

## Executar localmente

Requisitos: Node.js 22.12 ou superior e npm.

```bash
cd admin-web
npm install
npm run dev
```

Abra `http://localhost:5173`.

No primeiro acesso, o painel solicita a criação de um administrador local. A senha passa por PBKDF2 com SHA-256 e salt aleatório antes de ser armazenada; mesmo assim, autenticação somente no navegador não substitui um backend seguro.

## Validação

```bash
npm test
npm run typecheck
npm run build
```

O resultado de produção fica em `admin-web/dist`.

## Funcionalidades

- configuração e login de administrador local;
- dashboard com total, publicados, rascunhos e artigos recentes;
- pesquisa e filtros por categoria e status;
- criação, edição, duplicação, publicação e exclusão;
- autosave de rascunho;
- editor TipTap com títulos H1–H3, negrito, itálico, sublinhado, tachado, listas, citação, código, alinhamento, links, separador e imagens por URL;
- imagem de capa local de até 2 MB;
- visualização antes da publicação;
- sanitização do documento estruturado e restrição de links a HTTP/HTTPS;
- layout responsivo para computador, tablet e celular.

## Formato do artigo

O conteúdo usa JSON compatível com TipTap/ProseMirror e recebe a versão `tiptap-json-v1`. O Android possui um interpretador para os mesmos blocos e marcas. Metadados como título, categoria, autor, resumo, tags, status e datas ficam fora do documento.

## Limite desta versão

O IndexedDB pertence ao navegador e ao domínio usados. Portanto:

- limpar os dados do site remove a conta e os artigos;
- outro computador ou navegador não recebe os mesmos dados;
- o painel não sincroniza automaticamente com o Android;
- publicar significa alterar o status no armazenamento local.

Para sincronização real, implemente uma API que preserve as interfaces `ArticleRepository` e `AdminAuthRepository` existentes.
