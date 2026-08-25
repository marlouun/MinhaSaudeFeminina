import type { AdminSession } from '../types/article'
import { clearSupabaseSession, getCurrentUser, signInAdmin, type SupabaseUser } from '../supabase/client'
import type { AdminAuthRepository, AdminSetupInput } from './AdminAuthRepository'

function toSession(user: SupabaseUser): AdminSession {
  const metadataName = typeof user.user_metadata?.display_name === 'string' ? user.user_metadata.display_name.trim() : ''
  const email = user.email ?? ''
  return {
    adminId: user.id,
    displayName: metadataName || email.split('@')[0] || 'Administrador',
  }
}

export class SupabaseAdminAuthRepository implements AdminAuthRepository {
  async hasAdmin(): Promise<boolean> {
    // O administrador é criado manualmente no Supabase Auth; o painel não permite cadastro público.
    return true
  }

  async setup(_input: AdminSetupInput): Promise<AdminSession> {
    throw new Error('Crie o administrador em Supabase > Authentication > Users e depois entre por esta tela.')
  }

  async login(email: string, password: string): Promise<AdminSession> {
    return toSession(await signInAdmin(email, password))
  }

  logout(): void {
    clearSupabaseSession()
  }

  async getSession(): Promise<AdminSession | null> {
    const user = await getCurrentUser()
    return user ? toSession(user) : null
  }
}
