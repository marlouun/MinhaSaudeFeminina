import {
  createContext,
  type PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'
import { adminAuthRepository } from '../services'
import type { AdminSession } from '../types/article'

interface AuthContextValue {
  loading: boolean
  hasAdmin: boolean
  session: AdminSession | null
  setup(displayName: string, email: string, password: string): Promise<void>
  login(email: string, password: string): Promise<void>
  logout(): void
  refresh(): Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: PropsWithChildren) {
  const [loading, setLoading] = useState(true)
  const [hasAdmin, setHasAdmin] = useState(false)
  const [session, setSession] = useState<AdminSession | null>(null)

  const refresh = useCallback(async () => {
    setLoading(true)
    try {
      const [configured, currentSession] = await Promise.all([
        adminAuthRepository.hasAdmin(),
        adminAuthRepository.getSession(),
      ])
      setHasAdmin(configured)
      setSession(currentSession)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  const setup = useCallback(async (displayName: string, email: string, password: string) => {
    const createdSession = await adminAuthRepository.setup({ displayName, email, password })
    setHasAdmin(true)
    setSession(createdSession)
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    setSession(await adminAuthRepository.login(email, password))
  }, [])

  const logout = useCallback(() => {
    adminAuthRepository.logout()
    setSession(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ loading, hasAdmin, session, setup, login, logout, refresh }),
    [hasAdmin, loading, login, logout, refresh, session, setup],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth deve ser usado dentro de AuthProvider.')
  return context
}
