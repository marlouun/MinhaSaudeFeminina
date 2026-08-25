import type { ArticleDocument } from '../types/article'
import { normalizeSafeHttpUrl } from './url'

const allowedNodeTypes = new Set([
  'doc',
  'paragraph',
  'text',
  'heading',
  'bulletList',
  'orderedList',
  'listItem',
  'blockquote',
  'codeBlock',
  'horizontalRule',
  'hardBreak',
  'image',
])
const allowedMarkTypes = new Set(['bold', 'italic', 'underline', 'strike', 'code'])
const allowedAlignments = new Set(['left', 'center', 'right', 'justify'])

function safeImageSource(value: string): string | null {
  if (/^data:image\/(png|jpe?g|webp|gif);base64,/i.test(value)) return value
  return normalizeSafeHttpUrl(value)
}

function sanitizeMarks(value: unknown): Array<Record<string, unknown>> | undefined {
  if (!Array.isArray(value)) return undefined
  const marks: Array<Record<string, unknown>> = []
  for (const item of value) {
    if (!item || typeof item !== 'object') continue
    const mark = item as Record<string, unknown>
    const type = typeof mark.type === 'string' ? mark.type : ''
    if (allowedMarkTypes.has(type)) {
      marks.push({ type })
      continue
    }
    if (type === 'link') {
      const attrs = mark.attrs && typeof mark.attrs === 'object' ? mark.attrs as Record<string, unknown> : {}
      const href = normalizeSafeHttpUrl(typeof attrs.href === 'string' ? attrs.href : '')
      if (href) {
        marks.push({
          type: 'link',
          attrs: {
            href,
            target: '_blank',
            rel: 'noopener noreferrer nofollow',
          },
        })
      }
    }
  }
  return marks.length > 0 ? marks : undefined
}

function sanitizeNode(value: unknown): Record<string, unknown> | null {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return null
  const node = value as Record<string, unknown>
  const type = typeof node.type === 'string' ? node.type : ''
  if (!allowedNodeTypes.has(type)) return null

  if (type === 'text') {
    const result: Record<string, unknown> = {
      type: 'text',
      text: typeof node.text === 'string' ? node.text : '',
    }
    const marks = sanitizeMarks(node.marks)
    if (marks) result.marks = marks
    return result
  }

  if (type === 'image') {
    const attrs = node.attrs && typeof node.attrs === 'object' ? node.attrs as Record<string, unknown> : {}
    const source = safeImageSource(typeof attrs.src === 'string' ? attrs.src : '')
    if (!source) return null
    return {
      type: 'image',
      attrs: {
        src: source,
        alt: typeof attrs.alt === 'string' ? attrs.alt.slice(0, 200) : '',
        title: typeof attrs.title === 'string' ? attrs.title.slice(0, 200) : null,
      },
    }
  }

  const sanitized: Record<string, unknown> = { type }
  if (type === 'heading') {
    const attrs = node.attrs && typeof node.attrs === 'object' ? node.attrs as Record<string, unknown> : {}
    const level = typeof attrs.level === 'number' ? Math.min(3, Math.max(1, attrs.level)) : 2
    const textAlign = typeof attrs.textAlign === 'string' && allowedAlignments.has(attrs.textAlign)
      ? attrs.textAlign
      : null
    sanitized.attrs = { level, textAlign }
  } else if (type === 'paragraph') {
    const attrs = node.attrs && typeof node.attrs === 'object' ? node.attrs as Record<string, unknown> : {}
    const textAlign = typeof attrs.textAlign === 'string' && allowedAlignments.has(attrs.textAlign)
      ? attrs.textAlign
      : null
    sanitized.attrs = { textAlign }
  }

  if (Array.isArray(node.content)) {
    sanitized.content = node.content
      .map(sanitizeNode)
      .filter((child): child is Record<string, unknown> => child !== null)
  }
  return sanitized
}

export function sanitizeArticleDocument(document: ArticleDocument): ArticleDocument {
  const root = sanitizeNode(document)
  if (!root || root.type !== 'doc') return { type: 'doc', content: [] }
  return root as ArticleDocument
}
