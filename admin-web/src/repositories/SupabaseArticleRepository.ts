import type { Article, ArticleDocument } from '../types/article'
import { supabaseRest } from '../supabase/client'
import { cloneDocument, slugify } from '../utils/article'
import { sanitizeArticleDocument } from '../utils/document'
import type { ArticleRepository } from './ArticleRepository'

interface ArticleRow {
  id: string
  slug: string
  category: string | null
  title: string
  subtitle: string | null
  summary: string | null
  content_json: ArticleDocument
  author: string | null
  tags: string[] | null
  cover_url: string | null
  status: 'draft' | 'published'
  created_at: string
  updated_at: string
}

function fromRow(row: ArticleRow): Article {
  const updatedAt = Date.parse(row.updated_at)
  return {
    id: row.id,
    slug: row.slug,
    category: row.category ?? '',
    title: row.title,
    subtitle: row.subtitle ?? '',
    summary: row.summary ?? '',
    content: row.content_json,
    author: row.author ?? '',
    tags: row.tags ?? [],
    coverImage: row.cover_url,
    coverAlt: '',
    status: row.status,
    formatVersion: 'tiptap-json-v1',
    createdAt: Date.parse(row.created_at),
    updatedAt,
    publishedAt: row.status === 'published' ? updatedAt : null,
  }
}

function toRow(article: Article): Omit<ArticleRow, 'created_at' | 'updated_at'> & { created_at: string; updated_at: string } {
  const now = Date.now()
  return {
    id: article.id,
    slug: article.slug,
    category: article.category || null,
    title: article.title,
    subtitle: article.subtitle || null,
    summary: article.summary || null,
    content_json: article.content,
    author: article.author || null,
    tags: article.tags,
    cover_url: article.coverImage,
    status: article.status,
    created_at: new Date(article.createdAt || now).toISOString(),
    updated_at: new Date(now).toISOString(),
  }
}

export class SupabaseArticleRepository implements ArticleRepository {
  watchAll(onValue: (articles: Article[]) => void, onError?: (error: unknown) => void): () => void {
    let active = true
    const load = async () => {
      try {
        const articles = await this.getAll()
        if (active) onValue(articles)
      } catch (error) {
        if (active) onError?.(error)
      }
    }
    void load()
    const timer = window.setInterval(() => void load(), 5_000)
    return () => {
      active = false
      window.clearInterval(timer)
    }
  }

  async getAll(): Promise<Article[]> {
    const response = await supabaseRest('articles?select=*&order=updated_at.desc')
    return (await response.json() as ArticleRow[]).map(fromRow)
  }

  async getById(id: string): Promise<Article | undefined> {
    const response = await supabaseRest(`articles?select=*&id=eq.${encodeURIComponent(id)}&limit=1`)
    const rows = await response.json() as ArticleRow[]
    return rows[0] ? fromRow(rows[0]) : undefined
  }

  async save(article: Article): Promise<Article> {
    const clean: Article = {
      ...article,
      title: article.title.trim(),
      subtitle: article.subtitle.trim(),
      summary: article.summary.trim(),
      author: article.author.trim(),
      category: article.category.trim(),
      tags: [...new Set(article.tags.map((tag) => tag.trim()).filter(Boolean))].slice(0, 12),
      slug: slugify(article.slug || article.title) || `artigo-${article.id.slice(0, 8)}`,
      content: sanitizeArticleDocument(cloneDocument(article.content)),
      updatedAt: Date.now(),
      publishedAt: article.status === 'published' ? article.publishedAt ?? Date.now() : null,
    }

    const response = await supabaseRest('articles?on_conflict=id', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Prefer: 'resolution=merge-duplicates,return=representation',
      },
      body: JSON.stringify(toRow(clean)),
    })
    const rows = await response.json() as ArticleRow[]
    if (!rows[0]) throw new Error('O Supabase não retornou o artigo salvo.')
    return fromRow(rows[0])
  }

  async delete(id: string): Promise<void> {
    await supabaseRest(`articles?id=eq.${encodeURIComponent(id)}`, { method: 'DELETE' })
  }

  async duplicate(id: string): Promise<Article> {
    const source = await this.getById(id)
    if (!source) throw new Error('Artigo não encontrado.')
    const now = Date.now()
    return await this.save({
      ...source,
      id: crypto.randomUUID(),
      title: `Cópia de ${source.title}`.slice(0, 160),
      slug: `${source.slug}-copia-${crypto.randomUUID().slice(0, 6)}`,
      content: cloneDocument(source.content),
      status: 'draft',
      createdAt: now,
      updatedAt: now,
      publishedAt: null,
    })
  }

  async seedIfEmpty(): Promise<void> {
    // Os dados compartilhados pertencem ao Supabase; não criamos seeds locais no painel remoto.
  }
}
