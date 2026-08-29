import { Eye, EyeOff, LockKeyhole, ShieldCheck } from 'lucide-react'
import { type FormEvent, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { LoadingScreen } from '../components/LoadingScreen'
import { useAuth } from '../contexts/AuthContext'

function traduzirErroDeAcesso(msg: string): string {
  const m = msg.toLowerCase()
  if (m.includes('missing email or phone') || m.includes('missing email')) return 'Informe o e-mail.'
  if (m.includes('invalid email')) return 'E-mail inválido.'
  if (m.includes('invalid login credentials') || m.includes('invalid password') || m.includes('wrong password')) return 'E-mail ou senha incorretos.'
  if (m.includes('email not confirmed')) return 'E-mail ainda não confirmado.'
  if (m.includes('too many requests') || m.includes('rate limit')) return 'Muitas tentativas. Aguarde alguns segundos e tente novamente.'
  if (m.includes('network') || m.includes('fetch')) return 'Erro de conexão. Verifique sua internet.'
  if (msg) return msg
  return 'Não foi possível concluir o acesso.'
}

export function LoginPage() {
  const { loading, session, login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (loading) return <LoadingScreen label="Conectando ao Supabase..." />
  if (session) return <Navigate to="/" replace />

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(email, password)
      navigate('/', { replace: true })
    } catch (reason) {
      const raw = reason instanceof Error ? reason.message : ''
      setError(traduzirErroDeAcesso(raw))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-hero" aria-label="Apresentação do painel">
        <div className="login-brand">
          <div className="login-brand-icon"><img src="/minha-saude-feminina.svg" alt="" aria-hidden="true" /></div>
          <div>
            <strong>Minha Saúde Feminina</strong>
            <span>Painel administrativo</span>
          </div>
        </div>
        <div className="login-unifebe-logo">
          <img src="/unifebe-logo-transparent.png" alt="Unifebe — É Nossa. É Daqui." />
        </div>
        <div className="login-hero-content">
          <span className="eyebrow">Conteúdo e cuidado</span>
          <h1>Para a mulher<br />em todas as fases da vida.</h1>
          <p>Escreva, revise e publique conteúdos de saúde sincronizados com o aplicativo Minha Saúde Feminina.</p>
          <div className="login-feature"><ShieldCheck size={22} /><span>Artigos compartilhados pelo Supabase com o aplicativo.</span></div>
          <div className="login-feature"><LockKeyhole size={22} /><span>Acesso administrativo autenticado pelo Supabase.</span></div>
        </div>
      </section>

      <section className="login-panel">
        <div className="login-panel-deco" aria-hidden="true">
          <span className="deco-circle deco-circle-1" />
          <span className="deco-circle deco-circle-2" />
          <span className="deco-circle deco-circle-3" />
          <span className="deco-ring deco-ring-1" />
          <span className="deco-ring deco-ring-2" />
          <span className="deco-dot deco-dot-1" />
          <span className="deco-dot deco-dot-2" />
          <span className="deco-dot deco-dot-3" />
          <span className="deco-cross deco-cross-1" />
          <span className="deco-cross deco-cross-2" />
          <span className="deco-pill deco-pill-1" />
          <span className="deco-pill deco-pill-2" />
        </div>
        <div className="login-card">
          <div className="login-card-heading">
            <span className="login-card-icon"><LockKeyhole size={22} /></span>
            <div>
              <h2>Entrar no painel</h2>
              <p>Use uma conta cadastrada em Supabase Authentication.</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} noValidate>
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
                placeholder="admin@email.com"
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
                  autoComplete="current-password"
                  required
                  minLength={8}
                  placeholder="Sua senha administrativa"
                />
                <button type="button" aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'} onClick={() => setShowPassword((current) => !current)}>
                  {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                </button>
              </div>
            </div>

            {error && <div className="alert alert-danger" role="alert">{error}</div>}

            <button type="submit" className="btn btn-primary login-submit" disabled={submitting}>
              {submitting && <span className="spinner-border spinner-border-sm" aria-hidden="true" />}
              {submitting ? 'Conectando...' : 'Entrar'}
            </button>
          </form>

          <div className="local-security-note">
            <ShieldCheck size={18} />
            <span>Somente contas administrativas autorizadas devem ter permissão para criar, editar e excluir artigos.</span>
          </div>
        </div>
      </section>
    </main>
  )
}
