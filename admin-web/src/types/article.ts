export type ArticleStatus = 'draft' | 'published'

export interface ArticleDocument {
  type: 'doc'
  content?: Array<Record<string, unknown>>
  [key: string]: unknown
}

export interface Article {
  id: string
  slug: string
  category: string
  title: string
  subtitle: string
  summary: string
  content: ArticleDocument
  author: string
  tags: string[]
  coverImage: string | null
  coverAlt: string
  status: ArticleStatus
  formatVersion: 'tiptap-json-v1'
  createdAt: number
  updatedAt: number
  publishedAt: number | null
}

export interface ArticleFilters {
  search?: string
  category?: string
  status?: ArticleStatus | 'all'
}

export interface AdminCredential {
  id: string
  displayName: string
  email: string
  emailNormalized: string
  passwordHash: string
  passwordSalt: string
  passwordIterations: number
  createdAt: number
  updatedAt: number
}

export interface AdminSession {
  adminId: string
  displayName: string
}

export const emptyArticleDocument: ArticleDocument = {
  type: 'doc',
  content: [
    {
      type: 'paragraph',
      content: [],
    },
  ],
}
