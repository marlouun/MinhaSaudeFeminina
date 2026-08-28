# Correção da URL do Supabase

A integração de artigos estava apontando para um hostname inválido do projeto Supabase por causa de um caractere extra na URL.

URL correta:

```text
https://fjkbdpifozkfacgoqixl.supabase.co
```

A correção foi aplicada tanto no painel administrativo quanto no Android, para que login administrativo, CRUD de artigos e leitura de artigos publicados usem o mesmo projeto Supabase.
