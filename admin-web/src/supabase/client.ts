const SUPABASE_URL = 'https://fjkbdpifozkfacgoqixl.supabase.co'
const SUPABASE_PUBLISHABLE_KEY = 'sb_publishable_fqM-bD0er0xbJ_CeE9kLiQ_pWUBtd-t'
const SESSION_KEY = 'minha-saude-feminina-supabase-admin-session-v1'

interface StoredSession {
  accessToken: string
  refreshToken: string
  expiresAt: number
}

interface TokenResponse {
  access_token: string
  refresh_token: string
  expires_in: number
  user?: SupabaseUser
}

export interface SupabaseUser {
  id: string
  email?: string
  user_metadata?: Record<string, unknown>
}

function readSession(): StoredSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredSession
  } catch {
    localStorage.removeItem(SESSION_KEY)
    return null
  }
}

function saveTokenResponse(response: TokenResponse): StoredSession {
  const session: StoredSession = {
    accessToken: response.access_token,
    refreshToken: response.refresh_token,
    expiresAt: Date.now() + response.expires_in * 1000,
  }
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  return session
}

async function parseError(response: Response): Promise<Error> {
  try {
    const body = await response.json() as { message?: string; msg?: string; error_description?: string; error?: string }
    return new Error(body.message || body.msg || body.error_description || body.error || `Erro ${response.status} ao acessar o Supabase.`)
  } catch {
    return new Error(`Erro ${response.status} ao acessar o Supabase.`)
  }
}

export async function signInAdmin(email: string, password: string): Promise<SupabaseUser> {
  const response = await fetch(`${SUPABASE_URL}/auth/v1/token?grant_type=password`, {
    method: 'POST',
    headers: {
      apikey: SUPABASE_PUBLISHABLE_KEY,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email: email.trim(), password }),
  })
  if (!response.ok) throw await parseError(response)
  const payload = await response.json() as TokenResponse
  saveTokenResponse(payload)
  if (payload.user) return payload.user
  const user = await getCurrentUser()
  if (!user) throw new Error('Não foi possível carregar o usuário administrativo.')
  return user
}

async function refreshSession(session: StoredSession): Promise<StoredSession> {
  const response = await fetch(`${SUPABASE_URL}/auth/v1/token?grant_type=refresh_token`, {
    method: 'POST',
    headers: {
      apikey: SUPABASE_PUBLISHABLE_KEY,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refresh_token: session.refreshToken }),
  })
  if (!response.ok) {
    clearSupabaseSession()
    throw await parseError(response)
  }
  return saveTokenResponse(await response.json() as TokenResponse)
}

export async function getAccessToken(): Promise<string | null> {
  let session = readSession()
  if (!session) return null
  if (session.expiresAt <= Date.now() + 30_000) session = await refreshSession(session)
  return session.accessToken
}

export async function getCurrentUser(): Promise<SupabaseUser | null> {
  const token = await getAccessToken()
  if (!token) return null
  const response = await fetch(`${SUPABASE_URL}/auth/v1/user`, {
    headers: {
      apikey: SUPABASE_PUBLISHABLE_KEY,
      Authorization: `Bearer ${token}`,
    },
  })
  if (response.status === 401) {
    clearSupabaseSession()
    return null
  }
  if (!response.ok) throw await parseError(response)
  return await response.json() as SupabaseUser
}

export function clearSupabaseSession(): void {
  localStorage.removeItem(SESSION_KEY)
}

export async function supabaseRest(path: string, init: RequestInit = {}, requireAuth = true): Promise<Response> {
  const token = requireAuth ? await getAccessToken() : null
  if (requireAuth && !token) throw new Error('Sua sessão administrativa expirou. Entre novamente.')

  const response = await fetch(`${SUPABASE_URL}/rest/v1/${path}`, {
    ...init,
    headers: {
      apikey: SUPABASE_PUBLISHABLE_KEY,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init.headers ?? {}),
    },
  })
  if (!response.ok) throw await parseError(response)
  return response
}
