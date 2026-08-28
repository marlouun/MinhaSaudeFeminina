import { Eye, EyeOff, HeartPulse, LockKeyhole, ShieldCheck } from 'lucide-react'
import { type FormEvent, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { LoadingScreen } from '../components/LoadingScreen'
import { useAuth } from '../contexts/AuthContext'

export function LoginPage() {
  const { loading, hasAdmin, session, setup, login } = useAuth()
  const navigate = useNavigate()
  const [displayName, setDisplayName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (loading) return <LoadingScreen label="Abrindo o painel local..." />
  if (session) return <Navigate to="/" replace />

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    if (!hasAdmin && password !== confirmation) {
      setError('As senhas não conferem.')
      return
    }

    setSubmitting(true)
    try {
      if (hasAdmin) await login(email, password)
      else await setup(displayName, email, password)
      navigate('/', { replace: true })
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Não foi possível concluir o acesso.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-hero" aria-label="Apresentação do painel">
        <div className="login-brand">
          <div className="login-brand-icon"><HeartPulse size={31} /></div>
          <div>
            <strong>Minha Saúde Feminina</strong>
            <span>Painel de conteúdo</span>
          </div>
        </div>
        <div className="login-hero-content">
          <span className="eyebrow">Administração local</span>
          <h1>Informação de saúde com organização e cuidado.</h1>
          <p>Crie, revise e publique artigos em um editor completo, mantendo os dados somente neste navegador enquanto o backend não é definido.</p>
          <div className="login-feature"><ShieldCheck size={22} /><span>Nenhuma credencial é enviada para um serviço externo.</span></div>
          <div className="login-feature"><LockKeyhole size={22} /><span>A senha é derivada com PBKDF2 e salt aleatório antes de ser salva.</span></div>
        </div>
      </section>

      <section className="login-panel">
        <div className="login-card">
          <div className="login-card-heading">
            <span className="login-card-icon"><LockKeyhole size={22} /></span>
            <div>
              <h2>{hasAdmin ? 'Entrar no painel' : 'Configurar administrador'}</h2>
              <p>{hasAdmin ? 'Use a conta criada neste navegador.' : 'Esta etapa acontece apenas no primeiro acesso.'}</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} noValidate>
            {!hasAdmin && (
              <div className="form-field">
                <label htmlFor="display-name">Nome do administrador</label>
                <input
                  id="display-name"
                  className="form-control"
                  value={displayName}
                  onChange={(event) => setDisplayName(event.target.value.slice(0, 80))}
                  autoComplete="name"
                  required
                  minLength={2}
                  placeholder="Ex.: Marlon"
                />
              </div>
            )}

            <div className="form-field">
              <label htmlFor="admin-email">E-mail</label>
              <input
                id="admin-email"
                className="form-control"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value.slice(0, 254))}
                autoComplete="username"
                required
                placeholder="admin@exemplo.com"
              />
            </div>

            <div className="form-field">
              <label htmlFor="admin-password">Senha</label>
              <div className="password-field">
                <input
                  id="admin-password"
                  className="form-control"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(event) => setPassword(event.target.value.slice(0, 128))}
                  autoComplete={hasAdmin ? 'current-password' : 'new-password'}
                  required
                  minLength={8}
                  placeholder="Mínimo de 8 caracteres"
                />
                <button type="button" aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'} onClick={() => setShowPassword((current) => !current)}>
                  {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                </button>
              </div>
            </div>

            {!hasAdmin && (
              <div className="form-field">
                <label htmlFor="admin-confirmation">Confirmar senha</label>
                <input
                  id="admin-confirmation"
                  className="form-control"
                  type={showPassword ? 'text' : 'password'}
                  value={confirmation}
                  onChange={(event) => setConfirmation(event.target.value.slice(0, 128))}
                  autoComplete="new-password"
                  required
                  minLength={8}
                />
                <small>Use pelo menos uma letra e um número.</small>
              </div>
            )}

            {error && <div className="alert alert-danger" role="alert">{error}</div>}

            <button type="submit" className="btn btn-primary login-submit" disabled={submitting}>
              {submitting && <span className="spinner-border spinner-border-sm" aria-hidden="true" />}
              {submitting ? 'Processando...' : hasAdmin ? 'Entrar' : 'Criar administrador local'}
            </button>
          </form>

          <div className="local-security-note">
            <ShieldCheck size={18} />
            <span>Modo temporário: apagar os dados do navegador também remove a conta e os artigos locais.</span>
          </div>
        </div>
      </section>
    </main>
  )
}
