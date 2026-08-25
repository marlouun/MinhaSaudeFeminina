import { describe, expect, it } from 'vitest'
import type { Article } from '../types/article'
import { parseTags, slugify, validateArticle } from './article'

function validArticle(): Article {
  return {
    id: 'article-1',
    slug: 'saude-feminina',
    category: 'Prevenção',
    title: 'Saúde feminina e prevenção',
    subtitle: 'Informações para apoiar o cuidado.',
    summary: 'Um resumo suficientemente completo para permitir a publicação do conteúdo.',
    content: {
      type: 'doc',
      content: [{ type: 'paragraph', content: [{ type: 'text', text: 'Conteúdo do artigo.' }] }],
    },
    author: 'Equipe',
    tags: ['prevenção'],
    coverImage: null,
    coverAlt: '',
    status: 'draft',
    formatVersion: 'tiptap-json-v1',
    createdAt: 1,
    updatedAt: 1,
    publishedAt: null,
  }
}

describe('utilitários de artigo', () => {
  it('gera slug sem acentos e símbolos', () => {
    expect(slugify('Ciclo Menstrual: dúvidas & cuidados')).toBe('ciclo-menstrual-duvidas-cuidados')
  })

  it('remove tags repetidas e limita o total', () => {
    expect(parseTags('Ciclo, ciclo, #Saúde,  prevenção ')).toEqual(['Ciclo', 'Saúde', 'prevenção'])
    expect(parseTags(Array.from({ length: 20 }, (_, index) => `tag${index}`).join(',')).length).toBe(12)
  })

  it('valida requisitos adicionais para publicação', () => {
    const article = validArticle()
    expect(validateArticle(article, true).valid).toBe(true)
    expect(validateArticle({ ...article, summary: '', content: { type: 'doc', content: [] } }, true).valid).toBe(false)
  })
})
