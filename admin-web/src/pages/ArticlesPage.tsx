import {
  Copy,
  Edit3,
  Eye,
  FilePlus2,
  Files,
  Search,
  Send,
  Trash2,
  Undo2,
} from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { LoadingScreen } from '../components/LoadingScreen'
import { useArticles } from '../hooks/useArticles'
import { articleRepository } from '../services'
import type { Article, ArticleStatus } from '../types/article'
import { formatDate, validateArticle } from '../utils/article'

export function ArticlesPage() {
  const navigate = useNavigate()
  const { articles, loading, error } = useArticles()
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState<ArticleStatus | 'all'>('all')
  const [category, setCategory] = useState('all')
  const [deleteTarget, setDeleteTarget] = useState<Article | null>(null)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const categories = useMemo(
    () => [...new Set(articles.map((article) => article.category).filter(Boolean))].sort((a, b) => a.localeCompare(b, 'pt-BR')),
    [articles],
  )

  const filtered = useMemo(() => {
    const term = search.trim().toLocaleLowerCase('pt-BR')
    return articles.filter((article) => {
      const matchesStatus = status === 'all' || article.status === status
      const matchesCategory = category === 'all' || article.category === category
      const searchable = [article.title, article.subtitle, article.summary, article.author, article.tags.join(' ')].join(' ').toLocaleLowerCase('pt-BR')
      return matchesStatus && matchesCategory && (!term || searchable.includes(term))
    })
  }, [articles, category, search, status])

  if (loading) return <LoadingScreen label="Carregando artigos..." />

  const runAction = async (action: () => Promise<void>, successMessage: string) => {
    setBusy(true)
    setNotice(null)
    setActionError(null)
    try {
      await action()
      setNotice(successMessage)
    } catch (reason) {
      setActionError(reason instanceof Error ? reason.message : 'Não foi possível concluir a operação.')
    } finally {
      setBusy(false)
    }
  }

  const handleDuplicate = (article: Article) => {
    void runAction(async () => {
      const duplicated = await articleRepository.duplicate(article.id)
      navigate(`/articles/${duplicated.id}/edit`)
    }, 'Artigo duplicado como rascunho.')
  }

  const handleStatus = (article: Article) => {
    const nextStatus: ArticleStatus = article.status === 'published' ? 'draft' : 'published'
    if (nextStatus === 'published') {
      const validation = validateArticle({ ...article, status: nextStatus }, true)
      if (!validation.valid) {
        setActionError(validation.errors.join(' '))
        return
      }
    }
    void runAction(
      async () => { await articleRepository.save({ ...article, status: nextStatus }) },
      nextStatus === 'published' ? 'Artigo publicado.' : 'Artigo voltou para rascunho.',
    )
  }

  const confirmDelete = () => {
    if (!deleteTarget) return
    const target = deleteTarget
    void runAction(async () => {
      await articleRepository.delete(target.id)
      setDeleteTarget(null)
    }, 'Artigo excluído deste navegador.')
  }

  return (
    <div className="page-stack">
      <section className="page-heading">
        <div>
          <span className="eyebrow">Conteúdo</span>
          <h1>Artigos</h1>
          <p>Pesquise, edite, duplique, publique ou exclua conteúdos locais.</p>
        </div>
        <Link className="btn btn-primary" to="/articles/new"><FilePlus2 size={18} /> Novo artigo</Link>
      </section>

      {(error || actionError) && <div className="alert alert-danger" role="alert">{error || actionError}</div>}
      {notice && <div className="alert alert-success" role="status">{notice}</div>}

      <section className="panel-card filters-panel" aria-label="Filtros de artigos">
        <div className="search-field">
          <Search size={18} />
          <input
            type="search"
            value={search}
            onChange={(event) => setSearch(event.target.value.slice(0, 120))}
            placeholder="Pesquisar título, resumo, autor ou tag"
            aria-label="Pesquisar artigos"
          />
        </div>
        <select className="form-select" value={status} onChange={(event) => setStatus(event.target.value as ArticleStatus | 'all')} aria-label="Filtrar por status">
          <option value="all">Todos os status</option>
          <option value="published">Publicados</option>
          <option value="draft">Rascunhos</option>
        </select>
        <select className="form-select" value={category} onChange={(event) => setCategory(event.target.value)} aria-label="Filtrar por categoria">
          <option value="all">Todas as categorias</option>
          {categories.map((item) => <option key={item} value={item}>{item}</option>)}
        </select>
      </section>

      <section className="panel-card articles-panel">
        <div className="articles-count">{filtered.length} de {articles.length} artigo(s)</div>
        {filtered.length === 0 ? (
          <div className="empty-state"><Files size={44} /><h2>Nenhum artigo encontrado</h2><p>Ajuste os filtros ou crie um conteúdo novo.</p></div>
        ) : (
          <div className="table-responsive">
            <table className="articles-table">
              <thead><tr><th>Artigo</th><th>Categoria</th><th>Status</th><th>Atualizado</th><th aria-label="Ações" /></tr></thead>
              <tbody>
                {filtered.map((article) => (
                  <tr key={article.id}>
                    <td>
                      <div className="article-cell">
                        <strong>{article.title || 'Artigo sem título'}</strong>
                        <span>{article.summary || 'Sem resumo'}</span>
                      </div>
                    </td>
                    <td><span className="category-pill">{article.category || 'Sem categoria'}</span></td>
                    <td><span className={`status-badge ${article.status}`}>{article.status === 'published' ? 'Publicado' : 'Rascunho'}</span></td>
                    <td className="date-cell">{formatDate(article.updatedAt)}</td>
                    <td>
                      <div className="row-actions">
                        <Link className="icon-button" title="Editar" aria-label={`Editar ${article.title}`} to={`/articles/${article.id}/edit`}><Edit3 size={17} /></Link>
                        <Link className="icon-button" title="Visualizar" aria-label={`Visualizar ${article.title}`} to={`/articles/${article.id}/preview`}><Eye size={17} /></Link>
                        <button type="button" className="icon-button" title="Duplicar" aria-label={`Duplicar ${article.title}`} onClick={() => handleDuplicate(article)} disabled={busy}><Copy size={17} /></button>
                        <button type="button" className="icon-button" title={article.status === 'published' ? 'Voltar para rascunho' : 'Publicar'} aria-label={article.status === 'published' ? `Voltar ${article.title} para rascunho` : `Publicar ${article.title}`} onClick={() => handleStatus(article)} disabled={busy}>
                          {article.status === 'published' ? <Undo2 size={17} /> : <Send size={17} />}
                        </button>
                        <button type="button" className="icon-button danger" title="Excluir" aria-label={`Excluir ${article.title}`} onClick={() => setDeleteTarget(article)} disabled={busy}><Trash2 size={17} /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <ConfirmDialog
        open={deleteTarget !== null}
        title="Excluir artigo?"
        message={`“${deleteTarget?.title || 'Artigo sem título'}” será removido somente deste navegador. A ação não pode ser desfeita.`}
        confirmLabel="Excluir artigo"
        danger
        busy={busy}
        onConfirm={confirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
