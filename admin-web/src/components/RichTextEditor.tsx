import { EditorContent, useEditor } from '@tiptap/react'
import {
  AlignCenter,
  AlignJustify,
  AlignLeft,
  AlignRight,
  Bold,
  Code2,
  Heading1,
  Heading2,
  Heading3,
  ImagePlus,
  Italic,
  Link2,
  List,
  ListOrdered,
  Minus,
  Pilcrow,
  Quote,
  Redo2,
  RemoveFormatting,
  Strikethrough,
  Underline,
  Undo2,
  Unlink,
} from 'lucide-react'
import { type ReactNode, useEffect } from 'react'
import type { ArticleDocument } from '../types/article'
import { sanitizeArticleDocument } from '../utils/document'
import { normalizeSafeHttpUrl } from '../utils/url'
import { createEditorExtensions } from '../editor/extensions'

interface RichTextEditorProps {
  content: ArticleDocument
  onChange(content: ArticleDocument): void
  disabled?: boolean
}

interface ToolbarButtonProps {
  label: string
  active?: boolean
  disabled?: boolean
  onClick(): void
  children: ReactNode
}

function ToolbarButton({ label, active = false, disabled = false, onClick, children }: ToolbarButtonProps) {
  return (
    <button
      type="button"
      className={`editor-toolbar-button${active ? ' is-active' : ''}`}
      aria-label={label}
      title={label}
      aria-pressed={active}
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </button>
  )
}

export function RichTextEditor({ content, onChange, disabled = false }: RichTextEditorProps) {
  const editor = useEditor({
    extensions: createEditorExtensions(),
    content: sanitizeArticleDocument(content),
    editable: !disabled,
    immediatelyRender: false,
    onUpdate: ({ editor: currentEditor }) => {
      onChange(sanitizeArticleDocument(currentEditor.getJSON() as ArticleDocument))
    },
  })

  useEffect(() => {
    editor?.setEditable(!disabled)
  }, [disabled, editor])

  useEffect(() => {
    if (!editor) return
    const next = sanitizeArticleDocument(content)
    if (JSON.stringify(editor.getJSON()) !== JSON.stringify(next)) {
      editor.commands.setContent(next, { emitUpdate: false })
    }
  }, [content, editor])

  if (!editor) return <div className="editor-loading">Carregando editor...</div>

  const addLink = () => {
    const previous = String(editor.getAttributes('link').href ?? '')
    const value = window.prompt('Digite o endereço do link:', previous || 'https://')
    if (value === null) return
    if (!value.trim()) {
      editor.chain().focus().extendMarkRange('link').unsetLink().run()
      return
    }
    const safeUrl = normalizeSafeHttpUrl(value)
    if (!safeUrl) {
      window.alert('Use um endereço HTTP ou HTTPS válido.')
      return
    }
    editor.chain().focus().extendMarkRange('link').setLink({ href: safeUrl }).run()
  }

  const addImage = () => {
    const value = window.prompt('Digite o endereço HTTPS da imagem:')
    if (value === null) return
    const safeUrl = normalizeSafeHttpUrl(value)
    if (!safeUrl) {
      window.alert('Use um endereço HTTP ou HTTPS válido.')
      return
    }
    const alternativeText = window.prompt('Texto alternativo da imagem:', '') ?? ''
    editor.chain().focus().setImage({ src: safeUrl, alt: alternativeText.slice(0, 200) }).run()
  }

  return (
    <div className="rich-editor" aria-label="Editor de conteúdo do artigo">
      <div className="editor-toolbar" role="toolbar" aria-label="Formatação do texto">
        <ToolbarButton label="Texto normal" active={editor.isActive('paragraph')} onClick={() => editor.chain().focus().setParagraph().run()}><Pilcrow size={17} /></ToolbarButton>
        <ToolbarButton label="Título H1" active={editor.isActive('heading', { level: 1 })} onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}><Heading1 size={17} /></ToolbarButton>
        <ToolbarButton label="Título H2" active={editor.isActive('heading', { level: 2 })} onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}><Heading2 size={17} /></ToolbarButton>
        <ToolbarButton label="Título H3" active={editor.isActive('heading', { level: 3 })} onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}><Heading3 size={17} /></ToolbarButton>
        <span className="editor-toolbar-separator" />
        <ToolbarButton label="Negrito" active={editor.isActive('bold')} onClick={() => editor.chain().focus().toggleBold().run()}><Bold size={17} /></ToolbarButton>
        <ToolbarButton label="Itálico" active={editor.isActive('italic')} onClick={() => editor.chain().focus().toggleItalic().run()}><Italic size={17} /></ToolbarButton>
        <ToolbarButton label="Sublinhado" active={editor.isActive('underline')} onClick={() => editor.chain().focus().toggleUnderline().run()}><Underline size={17} /></ToolbarButton>
        <ToolbarButton label="Tachado" active={editor.isActive('strike')} onClick={() => editor.chain().focus().toggleStrike().run()}><Strikethrough size={17} /></ToolbarButton>
        <ToolbarButton label="Código" active={editor.isActive('code')} onClick={() => editor.chain().focus().toggleCode().run()}><Code2 size={17} /></ToolbarButton>
        <span className="editor-toolbar-separator" />
        <ToolbarButton label="Lista com marcadores" active={editor.isActive('bulletList')} onClick={() => editor.chain().focus().toggleBulletList().run()}><List size={17} /></ToolbarButton>
        <ToolbarButton label="Lista numerada" active={editor.isActive('orderedList')} onClick={() => editor.chain().focus().toggleOrderedList().run()}><ListOrdered size={17} /></ToolbarButton>
        <ToolbarButton label="Citação" active={editor.isActive('blockquote')} onClick={() => editor.chain().focus().toggleBlockquote().run()}><Quote size={17} /></ToolbarButton>
        <ToolbarButton label="Bloco de código" active={editor.isActive('codeBlock')} onClick={() => editor.chain().focus().toggleCodeBlock().run()}><Code2 size={17} /></ToolbarButton>
        <ToolbarButton label="Separador" onClick={() => editor.chain().focus().setHorizontalRule().run()}><Minus size={17} /></ToolbarButton>
        <span className="editor-toolbar-separator" />
        <ToolbarButton label="Adicionar ou editar link" active={editor.isActive('link')} onClick={addLink}><Link2 size={17} /></ToolbarButton>
        <ToolbarButton label="Remover link" disabled={!editor.isActive('link')} onClick={() => editor.chain().focus().unsetLink().run()}><Unlink size={17} /></ToolbarButton>
        <ToolbarButton label="Adicionar imagem por endereço" onClick={addImage}><ImagePlus size={17} /></ToolbarButton>
        <span className="editor-toolbar-separator" />
        <ToolbarButton label="Alinhar à esquerda" active={editor.isActive({ textAlign: 'left' })} onClick={() => editor.chain().focus().setTextAlign('left').run()}><AlignLeft size={17} /></ToolbarButton>
        <ToolbarButton label="Centralizar" active={editor.isActive({ textAlign: 'center' })} onClick={() => editor.chain().focus().setTextAlign('center').run()}><AlignCenter size={17} /></ToolbarButton>
        <ToolbarButton label="Alinhar à direita" active={editor.isActive({ textAlign: 'right' })} onClick={() => editor.chain().focus().setTextAlign('right').run()}><AlignRight size={17} /></ToolbarButton>
        <ToolbarButton label="Justificar" active={editor.isActive({ textAlign: 'justify' })} onClick={() => editor.chain().focus().setTextAlign('justify').run()}><AlignJustify size={17} /></ToolbarButton>
        <span className="editor-toolbar-separator" />
        <ToolbarButton label="Limpar formatação" onClick={() => editor.chain().focus().unsetAllMarks().clearNodes().run()}><RemoveFormatting size={17} /></ToolbarButton>
        <ToolbarButton label="Desfazer" disabled={!editor.can().chain().focus().undo().run()} onClick={() => editor.chain().focus().undo().run()}><Undo2 size={17} /></ToolbarButton>
        <ToolbarButton label="Refazer" disabled={!editor.can().chain().focus().redo().run()} onClick={() => editor.chain().focus().redo().run()}><Redo2 size={17} /></ToolbarButton>
      </div>
      <EditorContent editor={editor} className="editor-content" />
    </div>
  )
}
