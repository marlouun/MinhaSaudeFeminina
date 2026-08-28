import { db } from '../db/database'
import { createPasswordDigest, normalizeEmail, verifyPassword } from '../security/crypto'
import type { AdminCredential, AdminSession } from '../types/article'
import type { AdminAuthRepository, AdminSetupInput } from './AdminAuthRepository'

const SESSION_KEY = 'minha-saude-feminina-admin-session-v1'
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validateSetup(input: AdminSetupInput): void {
  if (input.displayName.trim().length < 2) throw new Error('Informe um nome com pelo menos 2 caracteres.')
  if (!emailPattern.test(input.email.trim())) throw new Error('Informe um e-mail válido.')
  if (input.password.length < 8 || !/[A-Za-zÀ-ÿ]/.test(input.password) || !/\d/.test(input.password)) {
    throw new Error('A senha deve ter pelo menos 8 caracteres, uma letra e um número.')
  }
}

function saveSession(admin: AdminCredential): AdminSession {
  const session = { adminId: admin.id, displayName: admin.displayName }
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  return session
}

export class LocalAdminAuthRepository implements AdminAuthRepository {
  async hasAdmin(): Promise<boolean> {
    return (await db.admins.count()) > 0
  }

  async setup(input: AdminSetupInput): Promise<AdminSession> {
    validateSetup(input)
    if (await this.hasAdmin()) throw new Error('O administrador local já foi configurado.')

    const digest = await createPasswordDigest(input.password)
    const now = Date.now()
    const admin: AdminCredential = {
      id: crypto.randomUUID(),
      displayName: input.displayName.trim(),
      email: input.email.trim(),
      emailNormalized: normalizeEmail(input.email),
      passwordHash: digest.hash,
      passwordSalt: digest.salt,
      passwordIterations: digest.iterations,
      createdAt: now,
      updatedAt: now,
    }
    await db.admins.add(admin)
    return saveSession(admin)
  }

  async login(email: string, password: string): Promise<AdminSession> {
    const admin = await db.admins.where('emailNormalized').equals(normalizeEmail(email)).first()
    if (!admin) throw new Error('E-mail ou senha incorretos.')

    const valid = await verifyPassword(
      password,
      admin.passwordHash,
      admin.passwordSalt,
      admin.passwordIterations,
    )
    if (!valid) throw new Error('E-mail ou senha incorretos.')
    return saveSession(admin)
  }

  logout(): void {
    localStorage.removeItem(SESSION_KEY)
  }

  async getSession(): Promise<AdminSession | null> {
    const raw = localStorage.getItem(SESSION_KEY)
    if (!raw) return null
    try {
      const session = JSON.parse(raw) as AdminSession
      const admin = await db.admins.get(session.adminId)
      if (!admin) {
        this.logout()
        return null
      }
      return { adminId: admin.id, displayName: admin.displayName }
    } catch {
      this.logout()
      return null
    }
  }
}
