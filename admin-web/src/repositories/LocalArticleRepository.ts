import { liveQuery } from 'dexie'
import { db } from '../db/database'
import type { Article, ArticleDocument } from '../types/article'
import { cloneDocument, slugify } from '../utils/article'
import type { ArticleRepository } from './ArticleRepository'

function document(content: Array<Record<string, unknown>>): ArticleDocument {
  return { type: 'doc', content }
}

function demoArticles(): Article[] {
  const now = Date.now()
  return [
    {
      id: 'demo-ciclo-menstrual',
      slug: 'ciclo-menstrual-entenda-seu-padrao',
      category: 'Menstruação',
      title: 'Ciclo menstrual: entenda o seu padrão',
      subtitle: 'Um guia simples para observar duração, fluxo e mudanças importantes.',
      summary: 'Conhecer o próprio padrão ajuda a perceber mudanças e levar informações melhores à consulta.',
      content: document([
        { type: 'heading', attrs: { level: 2 }, content: [{ type: 'text', text: 'O que observar' }] },
        { type: 'paragraph', content: [{ type: 'text', text: 'O ciclo é contado do primeiro dia de uma menstruação até o dia anterior à próxima. O mais útil é conhecer o seu padrão e registrar mudanças.' }] },
        { type: 'bulletList', content: [
          { type: 'listItem', content: [{ type: 'paragraph', content: [{ type: 'text', text: 'data de início e término do sangramento' }] }] },
          { type: 'listItem', content: [{ type: 'paragraph', content: [{ type: 'text', text: 'intensidade do fluxo e presença de dor' }] }] },
          { type: 'listItem', content: [{ type: 'paragraph', content: [{ type: 'text', text: 'sangramento fora do padrão habitual' }] }] },
        ] },
        { type: 'blockquote', content: [{ type: 'paragraph', content: [{ type: 'text', text: 'O aplicativo ajuda a organizar informações, mas não realiza diagnóstico.' }] }] },
      ]),
      author: 'Equipe Minha Saúde Feminina',
      tags: ['ciclo', 'menstruação', 'autocuidado'],
      coverImage: null,
      coverAlt: '',
      status: 'published',
      formatVersion: 'tiptap-json-v1',
      createdAt: now - 3_000,
      updatedAt: now - 3_000,
      publishedAt: now - 3_000,
    },
    {
      id: 'demo-pre-natal',
      slug: 'pre-natal-por-que-comecar-cedo',
      category: 'Gestação',
      title: 'Pré-natal: por que começar cedo',
      subtitle: 'O acompanhamento organiza exames, vacinas e cuidados para a gestante e o bebê.',
      summary: 'Na suspeita ou confirmação de gravidez, procure a UBS para iniciar o acompanhamento.',
      content: document([
        { type: 'paragraph', content: [{ type: 'text', text: 'Ao suspeitar de gravidez, procure a UBS para confirmação e orientação. O acompanhamento precoce permite avaliar necessidades individuais.' }] },
        { type: 'heading', attrs: { level: 2 }, content: [{ type: 'text', text: 'Procure atendimento imediato se houver' }] },
        { type: 'bulletList', content: [
          { type: 'listItem', content: [{ type: 'paragraph', content: [{ type: 'text', text: 'sangramento ou dor forte' }] }] },
          { type: 'listItem', content: [{ type: 'paragraph', content: [{ type: 'text', text: 'febre, falta de ar ou desmaio' }] }] },
        ] },
      ]),
      author: 'Equipe Minha Saúde Feminina',
      tags: ['gestação', 'pré-natal', 'UBS'],
      coverImage: null,
      coverAlt: '',
      status: 'published',
      formatVersion: 'tiptap-json-v1',
      createdAt: now - 2_000,
      updatedAt: now - 2_000,
      publishedAt: now - 2_000,
    },
    {
      id: 'demo-rascunho-editor',
      slug: 'rascunho-de-demonstracao',
      category: 'Prevenção',
      title: 'Rascunho de demonstração',
      subtitle: 'Use este conteúdo para testar o editor sem alterar um artigo publicado.',
      summary: 'Este artigo foi criado como dado local de demonstração do painel.',
      content: document([
        { type: 'paragraph', content: [{ type: 'text', text: 'Edite este texto, teste títulos, listas, links e a visualização antes de publicar.' }] },
      ]),
      author: 'Equipe Minha Saúde Feminina',
      tags: ['demonstração'],
      coverImage: null,
      coverAlt: '',
      status: 'draft',
      formatVersion: 'tiptap-json-v1',
      createdAt: now - 1_000,
      updatedAt: now - 1_000,
      publishedAt: null,
    },
  ]
}

export class LocalArticleRepository implements ArticleRepository {
  watchAll(onValue: (articles: Article[]) => void, onError?: (error: unknown) => void): () => void {
    const subscription = liveQuery(() => db.articles.orderBy('updatedAt').reverse().toArray()).subscribe({
      next: onValue,
      error: (error) => onError?.(error),
    })
    return () => subscription.unsubscribe()
  }

  async getAll(): Promise<Article[]> {
    return await db.articles.orderBy('updatedAt').reverse().toArray()
  }

  async getById(id: string): Promise<Article | undefined> {
    return await db.articles.get(id)
  }

  async save(article: Article): Promise<Article> {
    const now = Date.now()
    const baseSlug = slugify(article.slug || article.title) || `artigo-${article.id.slice(0, 8)}`
    const uniqueSlug = await this.makeUniqueSlug(baseSlug, article.id)
    const saved: Article = {
      ...article,
      title: article.title.trim(),
      subtitle: article.subtitle.trim(),
      summary: article.summary.trim(),
      author: article.author.trim(),
      category: article.category.trim(),
      tags: [...new Set(article.tags.map((tag) => tag.trim()).filter(Boolean))].slice(0, 12),
      slug: uniqueSlug,
      content: cloneDocument(article.content),
      updatedAt: now,
      publishedAt: article.status === 'published' ? article.publishedAt ?? now : null,
    }
    await db.articles.put(saved)
    return saved
  }

  async delete(id: string): Promise<void> {
    await db.articles.delete(id)
  }

  async duplicate(id: string): Promise<Article> {
    const source = await this.getById(id)
    if (!source) throw new Error('Artigo não encontrado.')
    const now = Date.now()
    return await this.save({
      ...source,
      id: crypto.randomUUID(),
      title: `Cópia de ${source.title}`.slice(0, 160),
      slug: `${source.slug}-copia`,
      content: cloneDocument(source.content),
      status: 'draft',
      createdAt: now,
      updatedAt: now,
      publishedAt: null,
    })
  }

  async seedIfEmpty(): Promise<void> {
    if ((await db.articles.count()) === 0) await db.articles.bulkAdd(demoArticles())
  }

  private async makeUniqueSlug(base: string, currentId: string): Promise<string> {
    let candidate = base
    let suffix = 2
    while (true) {
      const owner = await db.articles.where('slug').equals(candidate).first()
      if (!owner || owner.id === currentId) return candidate
      candidate = `${base}-${suffix}`.slice(0, 140)
      suffix += 1
    }
  }
}
