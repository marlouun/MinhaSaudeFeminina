import Dexie, { type Table } from 'dexie'
import type { AdminCredential, Article } from '../types/article'

class AdminDatabase extends Dexie {
  articles!: Table<Article, string>
  admins!: Table<AdminCredential, string>

  constructor() {
    super('minha-saude-feminina-admin')

    this.version(1).stores({
      articles: '&id, &slug, status, category, updatedAt, createdAt',
      admins: '&id, &emailNormalized',
    })
  }
}

export const db = new AdminDatabase()
