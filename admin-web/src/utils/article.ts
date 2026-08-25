import type { Article, ArticleDocument } from '../types/article'
import { emptyArticleDocument } from '../types/article'

export interface ArticleValidationResult {
  valid: boolean
  errors: string[]
}

export function slugify(value: string): string {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('pt-BR')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 120)
}

export function cloneDocument(document: ArticleDocument): ArticleDocument {
  return JSON.parse(JSON.stringify(document)) as ArticleDocument
}

export function createEmptyArticle(): Article {
  const now = Date.now()
  return {
    id: crypto.randomUUID(),
    slug: '',
    category: 'Menstruação',
    title: '',
    subtitle: '',
    summary: '',
    content: cloneDocument(emptyArticleDocument),
    author: 'Equipe Minha Saúde Feminina',
    tags: [],
    coverImage: null,
    coverAlt: '',
    status: 'draft',
    formatVersion: 'tiptap-json-v1',
    createdAt: now,
    updatedAt: now,
    publishedAt: null,
  }
}

export function validateArticle(article: Article, forPublication = false): ArticleValidationResult {
  const errors: string[] = []
  if (!article.title.trim()) errors.push('Informe o título.')
  if (article.title.trim().length > 160) errors.push('O título pode ter no máximo 160 caracteres.')
  if (article.subtitle.length > 240) errors.push('O subtítulo pode ter no máximo 240 caracteres.')
  if (!article.category.trim()) errors.push('Informe a categoria.')
  if (!article.author.trim()) errors.push('Informe o autor.')
  if (article.summary.length > 400) errors.push('O resumo pode ter no máximo 400 caracteres.')
  if (forPublication && article.summary.trim().length < 20) {
    errors.push('Para publicar, escreva um resumo com pelo menos 20 caracteres.')
  }
  if (forPublication && !hasDocumentText(article.content)) {
    errors.push('Para publicar, escreva o conteúdo do artigo.')
  }
  if (article.tags.length > 12) errors.push('Use no máximo 12 tags.')
  return { valid: errors.length === 0, errors }
}

export function hasDocumentText(document: ArticleDocument): boolean {
  const visit = (value: unknown): boolean => {
    if (typeof value === 'string') return value.trim().length > 0
    if (Array.isArray(value)) return value.some(visit)
    if (value && typeof value === 'object') {
      const record = value as Record<string, unknown>
      if (typeof record.text === 'string' && record.text.trim().length > 0) return true
      return Object.values(record).some(visit)
    }
    return false
  }
  return visit(document.content ?? [])
}

export function formatDate(timestamp: number | null | undefined): string {
  if (!timestamp) return '—'
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(timestamp))
}

export function parseTags(value: string): string[] {
  const unique = new Map<string, string>()
  for (const part of value.split(',')) {
    const tag = part.trim().replace(/^#/, '').slice(0, 40)
    if (tag) unique.set(tag.toLocaleLowerCase('pt-BR'), tag)
  }
  return [...unique.values()].slice(0, 12)
}
