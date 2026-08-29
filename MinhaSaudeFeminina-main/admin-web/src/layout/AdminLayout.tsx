import {
  Cloud,
  FilePlus2,
  Files,
  LayoutDashboard,
  LogOut,
  Menu,
  ShieldCheck,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

const isNewArticleRoute = (pathname: string) =>
  pathname === '/articles/new' || pathname.startsWith('/articles/new/')

const navigation = [
  {
    to: '/',
    label: 'Dashboard',
    icon: LayoutDashboard,
    isActive: (pathname: string) => pathname === '/',
  },
  {
    to: '/articles',
    label: 'Artigos',
    icon: Files,
    isActive: (pathname: string) =>
      pathname === '/articles' || (pathname.startsWith('/articles/') && !isNewArticleRoute(pathname)),
  },
  {
    to: '/articles/new',
    label: 'Novo artigo',
    icon: FilePlus2,
    isActive: isNewArticleRoute,
  },
]

export function AdminLayout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const { session, logout } = useAuth()
  const navigate = useNavigate()
  const { pathname } = useLocation()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="admin-shell">
      {menuOpen && <button type="button" className="sidebar-backdrop" aria-label="Fechar menu" onClick={() => setMenuOpen(false)} />}
      <aside className={`admin-sidebar${menuOpen ? ' is-open' : ''}`}>
        <div className="sidebar-brand">
          <div className="brand-mark"><img src="/minha-saude-feminina.svg" alt="" aria-hidden="true" /></div>
          <div>
            <strong>Minha Saúde Feminina</strong>
            <span>Painel administrativo</span>
          </div>
          <button type="button" className="sidebar-mobile-close" aria-label="Fechar menu" onClick={() => setMenuOpen(false)}><X size={21} /></button>
        </div>

        <nav className="sidebar-nav" aria-label="Menu principal">
          {navigation.map(({ to, label, icon: Icon, isActive }) => {
            const active = isActive(pathname)
            return (
              <Link
                key={to}
                to={to}
                className={`sidebar-link${active ? ' active' : ''}`}
                aria-current={active ? 'page' : undefined}
                onClick={() => setMenuOpen(false)}
              >
                <Icon size={20} />
                <span>{label}</span>
              </Link>
            )
          })}
        </nav>

        <div className="sidebar-local-notice">
          <ShieldCheck size={21} />
          <div>
            <strong>Conectado ao Supabase</strong>
            <span>Artigos compartilhados entre painel e aplicativo.</span>
          </div>
        </div>

        <button type="button" className="sidebar-logout" onClick={handleLogout}>
          <LogOut size={19} /> Sair
        </button>
      </aside>

      <div className="admin-main">
        <header className="admin-topbar">
          <button type="button" className="menu-button" aria-label="Abrir menu" onClick={() => setMenuOpen(true)}><Menu size={22} /></button>
          <div className="topbar-title">
            <span>Minha Saúde Feminina</span>
            <small><Cloud size={13} /> Conteúdos sincronizados pelo Supabase</small>
          </div>
          <div className="topbar-user" title="Administrador conectado">
            <span>{session?.displayName?.charAt(0).toUpperCase() || 'A'}</span>
            <div>
              <strong>{session?.displayName || 'Administrador'}</strong>
              <small>Administrador</small>
            </div>
          </div>
        </header>
        <main className="admin-content"><Outlet /></main>
      </div>
    </div>
  )
}
