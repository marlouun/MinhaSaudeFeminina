# Modal de Mídias - Documentação

## Visão Geral

O componente `MediaModal` foi adicionado ao editor de artigos para permitir que administradores insiram imagens e vídeos de forma mais intuitiva através de um popup modal em vez de usar prompts simples.

## Funcionalidades Implementadas

### Inserção de Imagens

O modal oferece duas formas de adicionar imagens:

#### 1. Por URL
- Digite o endereço HTTP/HTTPS da imagem
- Adicione texto alternativo para acessibilidade
- Validação automática de URLs seguras

#### 2. Upload de Arquivo
- Selecione uma imagem do seu dispositivo (PNG, JPEG, GIF, WebP)
- Suporte para imagens até 2 MB
- Conversão automática para Data URL (base64)
- Possibilidade de integração futura com Supabase Storage

### Vídeos (Preparado para Implementação Futura)

- Interface já criada com tab "Vídeo"
- Estrutura pronta para suportar YouTube, Vimeo, etc.
- Aguarda extensão TipTap para vídeo

## Como Usar

### No Editor

1. Clique no botão de imagem (ícone ImagePlus) na barra de ferramentas do editor
2. O modal será aberto com duas abas: **Imagem** e **Vídeo**
3. Escolha o modo de inserção:
   - **URL da imagem**: Cole o link da imagem
   - **Fazer upload**: Selecione um arquivo do seu computador
4. Preencha o texto alternativo (importante para acessibilidade)
5. Clique em **Inserir**

### Atalhos de Teclado

- `ESC`: Fecha o modal
- Clicar fora do modal: Fecha o modal

## Arquivos Modificados

### Novos Arquivos

- `admin-web/src/components/MediaModal.tsx` - Componente do modal

### Arquivos Atualizados

- `admin-web/src/components/RichTextEditor.tsx` - Integração do modal
- `admin-web/src/styles.css` - Estilos do modal e animações

## Personalização

### Integrando Upload no Supabase

Para fazer upload das imagens diretamente no Supabase Storage em vez de usar Data URLs, você pode passar a prop `onUploadImage` para o `MediaModal`:

```typescript
<MediaModal
  isOpen={isMediaModalOpen}
  onClose={() => setIsMediaModalOpen(false)}
  onInsertImage={handleInsertImage}
  onUploadImage={async (file: File) => {
    // Fazer upload para Supabase Storage
    const publicUrl = await uploadPublicStorageObject(
      'article-images',
      `${Date.now()}-${file.name}`,
      file
    )
    return publicUrl
  }}
/>
```

### Validação de Tamanho

O limite de 2 MB é aplicado pela função `imageFileToDataUrl` em `utils/image.ts`. Para alterar:

```typescript
// Em admin-web/src/utils/image.ts
const maxSize = 2 * 1024 * 1024 // 2 MB - altere aqui
```

## Próximos Passos

### Suporte a Vídeos

Para adicionar suporte a vídeos:

1. Instalar extensão TipTap para vídeo ou iframe
2. Implementar parser de URLs do YouTube/Vimeo
3. Adicionar validação de URLs de vídeo
4. Atualizar o handler no modal para inserir vídeos

```typescript
// Exemplo de implementação futura
const handleInsertVideo = (url: string) => {
  const embedUrl = parseVideoUrl(url) // converter para embed
  editor.chain().focus().setIframe({ src: embedUrl }).run()
}
```

### Galeria de Imagens

Para adicionar uma galeria de imagens já enviadas:

1. Criar nova tab "Galeria" no modal
2. Listar imagens do Supabase Storage
3. Permitir seleção de imagem existente
4. Adicionar preview e busca

## Estilo e Animações

O modal inclui:
- Animação de fade-in no backdrop
- Animação de slide-up no card do modal
- Transições suaves em todos os botões e estados
- Design responsivo para mobile
- Tema consistente com o resto do painel

## Acessibilidade

- Atributos ARIA adequados (`role`, `aria-modal`, `aria-labelledby`)
- Foco gerenciado corretamente
- Navegação por teclado (ESC para fechar)
- Labels descritivos em todos os controles
- Campo de texto alternativo obrigatório para imagens
