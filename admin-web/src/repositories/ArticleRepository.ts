import type { Article } from '../types/article'

export interface ArticleRepository {
  watchAll(onValue: (articles: Article[]) => void, onError?: (error: unknown) => void): () => void
  getAll(): Promise<Article[]>
  getById(id: string): Promise<Article | undefined>
  save(article: Article): Promise<Article>
  delete(id: string): Promise<void>
  duplicate(id: string): Promise<Article>
  seedIfEmpty(): Promise<void>
}
