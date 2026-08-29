import { describe, expect, it } from 'vitest'
import type { ArticleDocument } from '../types/article'
import { sanitizeArticleDocument } from './document'

describe('sanitizeArticleDocument', () => {
  it('mantém formatação conhecida e remove links inseguros', () => {
    const input: ArticleDocument = {
      type: 'doc',
      content: [
        {
          type: 'paragraph',
          content: [
            {
              type: 'text',
              text: 'Link perigoso',
              marks: [
                { type: 'bold' },
                { type: 'link', attrs: { href: 'javascript:alert(1)' } },
              ],
            },
          ],
        },
      ],
    }

    const serialized = JSON.stringify(sanitizeArticleDocument(input))
    expect(serialized).toContain('bold')
    expect(serialized).not.toContain('javascript:')
  })

  it('normaliza links seguros e descarta nós desconhecidos', () => {
    const input: ArticleDocument = {
      type: 'doc',
      content: [
        {
          type: 'paragraph',
          content: [{ type: 'text', text: 'Site', marks: [{ type: 'link', attrs: { href: 'example.com' } }] }],
        },
        { type: 'script', content: [{ type: 'text', text: 'não permitido' }] },
      ],
    }

    const serialized = JSON.stringify(sanitizeArticleDocument(input))
    expect(serialized).toContain('https://example.com/')
    expect(serialized).not.toContain('script')
  })
})
