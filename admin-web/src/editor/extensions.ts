import Image from '@tiptap/extension-image'
import Placeholder from '@tiptap/extension-placeholder'
import TextAlign from '@tiptap/extension-text-align'
import StarterKit from '@tiptap/starter-kit'

interface ExtensionOptions {
  openLinks?: boolean
  placeholder?: string
}

export function createEditorExtensions(options: ExtensionOptions = {}) {
  return [
    StarterKit.configure({
      heading: {
        levels: [1, 2, 3],
      },
      link: {
        openOnClick: options.openLinks ?? false,
        autolink: true,
        linkOnPaste: true,
        defaultProtocol: 'https',
        HTMLAttributes: {
          target: '_blank',
          rel: 'noopener noreferrer nofollow',
        },
      },
    }),
    TextAlign.configure({
      types: ['heading', 'paragraph'],
      alignments: ['left', 'center', 'right', 'justify'],
    }),
    Image.configure({
      allowBase64: true,
      inline: false,
    }),
    Placeholder.configure({
      placeholder: options.placeholder ?? 'Comece a escrever o conteúdo do artigo...',
    }),
  ]
}
