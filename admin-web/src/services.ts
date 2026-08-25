import { LocalAdminAuthRepository } from './repositories/LocalAdminAuthRepository'
import { LocalArticleRepository } from './repositories/LocalArticleRepository'

export const adminAuthRepository = new LocalAdminAuthRepository()
export const articleRepository = new LocalArticleRepository()
