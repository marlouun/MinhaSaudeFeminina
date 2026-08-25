import { SupabaseAdminAuthRepository } from './repositories/SupabaseAdminAuthRepository'
import { SupabaseArticleRepository } from './repositories/SupabaseArticleRepository'

export const adminAuthRepository = new SupabaseAdminAuthRepository()
export const articleRepository = new SupabaseArticleRepository()
