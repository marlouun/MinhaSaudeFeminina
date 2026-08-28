import type { AdminSession } from '../types/article'

export interface AdminSetupInput {
  displayName: string
  email: string
  password: string
}

export interface AdminAuthRepository {
  hasAdmin(): Promise<boolean>
  setup(input: AdminSetupInput): Promise<AdminSession>
  login(email: string, password: string): Promise<AdminSession>
  logout(): void
  getSession(): Promise<AdminSession | null>
}
