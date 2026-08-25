import { FileEdit, FilePlus2, Files, Send, Sparkles } from 'lucide-react'
import { Link } from 'react-router-dom'
import { LoadingScreen } from '../components/LoadingScreen'
import { useArticles } from '../hooks/useArticles'
import { formatDate } from '../utils/article'

export function DashboardPage() {
  const { articles, loading, error } = useArticles()

  if (loading) return <LoadingScreen label="Carregando indicadores..." />

  const published = articles.filter((article) => article.status === 'published').length
  const drafts = articles.length - published
  const recent = articles.slice(0, 5)

  return (
    <div className="page-stack">
      <section className="page-heading dashboard-heading">
        <div>
          <span className="eyebrow">Visão geral</span>
          <h1>Dashboard</h1>
          <p>Acompanhe os artigos armazenados localmente neste navegador.</p>
        </div>
        <Link className="btn btn-primary" to="/articles/new"><FilePlus2 size={18} /> Novo artigo</Link>
      </section>

      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      <section className="stats-grid" aria-label="Resumo dos artigos">
        <article className="stat-card total">
          <span className="stat-icon"><Files size={24} /></span>
          <div><span>Total de artigos</span><strong>{articles.length}</strong></div>
        </article>
        <article className="stat-card published">
          <span className="stat-icon"><Send size={24} /></span>
          <div><span>Publicados</span><strong>{published}</strong></div>
        </article>
        <article className="stat-card drafts">
          <span className="stat-icon"><FileEdit size={24} /></span>
          <div><span>Rascunhos</span><strong>{drafts}</strong></div>
        </article>
      </section>

      <section className="dashboard-grid">
        <article className="panel-card recent-panel">
          <div className="panel-heading">
            <div><h2>Atualizados recentemente</h2><p>Últimas alterações feitas no painel.</p></div>
            <Link to="/articles">Ver todos</Link>
          </div>
          {recent.length === 0 ? (
            <div className="empty-state compact"><Files size={34} /><p>Nenhum artigo criado ainda.</p></div>
          ) : (
            <div className="recent-list">
              {recent.map((article) => (
                <Link to={`/articles/${article.id}/edit`} className="recent-item" key={article.id}>
                  <span className="recent-letter">{article.title.trim().charAt(0).toUpperCase() || 'A'}</span>
                  <div className="recent-content">
                    <strong>{article.title || 'Artigo sem título'}</strong>
                    <span>{article.category} · {formatDate(article.updatedAt)}</span>
                  </div>
                  <span className={`status-badge ${article.status}`}>{article.status === 'published' ? 'Publicado' : 'Rascunho'}</span>
                </Link>
              ))}
            </div>
          )}
        </article>

        <article className="panel-card getting-started">
          <span className="getting-started-icon"><Sparkles size={25} /></span>
          <h2>Fluxo recomendado</h2>
          <ol>
            <li>Crie o artigo e preencha os dados básicos.</li>
            <li>Formate o conteúdo e revise os links.</li>
            <li>Abra a visualização antes de publicar.</li>
            <li>Confirme fontes e linguagem de saúde.</li>
          </ol>
          <Link className="btn btn-outline-primary" to="/articles/new">Começar agora</Link>
        </article>
      </section>
    </div>
  )
}
