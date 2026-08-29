import {
  ArrowLeft,
  CheckCircle2,
  Clock3,
  Eye,
  FileEdit,
  ImagePlus,
  Save,
  Send,
  Trash2,
  TriangleAlert,
} from 'lucide-react'
import { type ChangeEvent, useCallback, useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { LoadingScreen } from '../components/LoadingScreen'
import { RichTextEditor } from '../components/RichTextEditor'
import { articleRepository } from '../services'
import type { Article, ArticleStatus } from '../types/article'
import {
  createEmptyArticle,
  formatDate,
  parseTags,
  slugify,
  validateArticle,
} from '../utils/article'
import { imageFileToDataUrl } from '../utils/image'

const suggestedCategories = [
  'Menstruação',
  'Gestação',
  'Prevenção',
  'Climatério e menopausa',
  'Saúde sexual',
  'Saúde mental',
  'Proteção',
]

type SaveState = 'idle' | 'pending' | 'saving' | 'saved' | 'error'

function articleFingerprint(article: Article): string {
  return JSON.stringify({
    slug: article.slug,
    category: article.category,
    title: article.title,
    subtitle: article.subtitle,
    summary: article.summary,
    content: article.content,
    author: article.author,
    tags: article.tags,
    coverImage: article.coverImage,
    coverAlt: article.coverAlt,
    status: article.status,
  })
}

function saveStateLabel(state: SaveState): string {
  switch (state) {
    case 'pending': return 'Alterações pendentes'
    case 'saving': return 'Salvando no navegador...'
    case 'saved': return 'Rascunho local atualizado'
    case 'error': return 'Falha ao salvar'
    default: return 'Pronto para editar'
  }
}

export function ArticleEditorPage() {
  const { articleId } = useParams()
  const navigate = useNavigate()
  const [article, setArticle] = useState<Article | null>(null)
  const [tagsText, setTagsText] = useState('')
  const [loading, setLoading] = useState(true)
  const [saveState, setSaveState] = useState<SaveState>('idle')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [validationErrors, setValidationErrors] = useState<string[]>([])
  const creatingRef = useRef(false)
  const lastSavedFingerprintRef = useRef('')
  const saveSequenceRef = useRef(0)

  const persist = useCallback(async (candidate: Article, successMessage?: string): Promise<Article | null> => {
    const sequence = ++saveSequenceRef.current
    setSaveState('saving')
    setError(null)
    try {
      const saved = await articleRepository.save(candidate)
      if (sequence !== saveSequenceRef.current) return saved
      lastSavedFingerprintRef.current = articleFingerprint(saved)
      setArticle(saved)
      setTagsText(saved.tags.join(', '))
      setSaveState('saved')
      if (successMessage) setMessage(successMessage)
      return saved
    } catch (reason) {
      if (sequence !== saveSequenceRef.current) return null
      setSaveState('error')
      setError(reason instanceof Error ? reason.message : 'Não foi possível salvar o artigo.')
      return null
    }
  }, [])

  useEffect(() => {
    let active = true
    setMessage(null)
    setError(null)
    setValidationErrors([])

    if (!articleId) {
      if (creatingRef.current) return
      creatingRef.current = true
      setLoading(true)
      const draft = createEmptyArticle()
      void articleRepository.save(draft)
        .then((saved) => {
          if (active) navigate(`/articles/${saved.id}/edit`, { replace: true })
        })
        .catch((reason: unknown) => {
          if (!active) return
          setError(reason instanceof Error ? reason.message : 'Não foi possível criar o rascunho.')
          setLoading(false)
        })
      return () => { active = false }
    }

    setLoading(true)
    void articleRepository.getById(articleId)
      .then((loaded) => {
        if (!active) return
        if (!loaded) {
          setError('Artigo não encontrado neste navegador.')
          return
        }
        setArticle(loaded)
        setTagsText(loaded.tags.join(', '))
        lastSavedFingerprintRef.current = articleFingerprint(loaded)
        setSaveState('idle')
      })
      .catch((reason: unknown) => {
        if (active) setError(reason instanceof Error ? reason.message : 'Não foi possível abrir o artigo.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [articleId, navigate])

  useEffect(() => {
    if (!article || loading) return
    const fingerprint = articleFingerprint(article)
    if (fingerprint === lastSavedFingerprintRef.current) return

    setSaveState('pending')
    const timer = window.setTimeout(() => {
      void persist(article)
    }, 900)
    return () => window.clearTimeout(timer)
  }, [article, loading, persist])

  useEffect(() => {
    const warnBeforeLeaving = (event: BeforeUnloadEvent) => {
      if (!article || articleFingerprint(article) === lastSavedFingerprintRef.current) return
      event.preventDefault()
      event.returnValue = ''
    }
    window.addEventListener('beforeunload', warnBeforeLeaving)
    return () => window.removeEventListener('beforeunload', warnBeforeLeaving)
  }, [article])

  if (loading) return <LoadingScreen label={articleId ? 'Abrindo o artigo...' : 'Criando rascunho...'} />

  if (!article) {
    return (
      <div className="page-stack">
        <div className="alert alert-danger" role="alert">{error || 'O artigo não pôde ser carregado.'}</div>
        <Link className="btn btn-light align-self-start" to="/articles"><ArrowLeft size={18} /> Voltar aos artigos</Link>
      </div>
    )
  }

  const patchArticle = (patch: Partial<Article>) => {
    setMessage(null)
    setValidationErrors([])
    setArticle((current) => current ? { ...current, ...patch } : current)
  }

  const changeTitle = (value: string) => {
    const nextTitle = value.slice(0, 160)
    const generatedSlug = !article.slug || article.slug === slugify(article.title)
      ? slugify(nextTitle)
      : article.slug
    patchArticle({ title: nextTitle, slug: generatedSlug })
  }

  const changeStatus = async (nextStatus: ArticleStatus) => {
    const candidate: Article = {
      ...article,
      status: nextStatus,
      publishedAt: nextStatus === 'draft' ? null : article.publishedAt,
    }
    if (nextStatus === 'published') {
      const validation = validateArticle(candidate, true)
      if (!validation.valid) {
        setValidationErrors(validation.errors)
        setError('Revise os campos destacados antes de publicar.')
        return
      }
    }
    setValidationErrors([])
    const saved = await persist(
      candidate,
      nextStatus === 'published' ? 'Artigo publicado localmente.' : 'Artigo salvo como rascunho.',
    )
    if (saved) setArticle(saved)
  }

  const openPreview = async () => {
    const saved = await persist(article)
    if (saved) navigate(`/articles/${saved.id}/preview`)
  }

  const handleCoverFile = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    try {
      patchArticle({ coverImage: await imageFileToDataUrl(file) })
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Não foi possível usar a imagem.')
    }
  }

  return (
    <div className="page-stack editor-page">
      <section className="page-heading editor-heading">
        <div>
          <Link className="back-link" to="/articles"><ArrowLeft size={17} /> Todos os artigos</Link>
          <span className="eyebrow">Editor de conteúdo</span>
          <h1>{article.title || 'Novo artigo'}</h1>
          <div className={`save-indicator ${saveState}`}>
            {saveState === 'saved' ? <CheckCircle2 size={15} /> : saveState === 'error' ? <TriangleAlert size={15} /> : <Clock3 size={15} />}
            {saveStateLabel(saveState)}
          </div>
        </div>
        <div className="heading-actions editor-actions">
          <button type="button" className="btn btn-light" onClick={() => void persist(article)} disabled={saveState === 'saving'}><Save size={18} /> Salvar</button>
          <button type="button" className="btn btn-outline-primary" onClick={() => void openPreview()} disabled={saveState === 'saving'}><Eye size={18} /> Visualizar</button>
          {article.status === 'published' ? (
            <button type="button" className="btn btn-secondary" onClick={() => void changeStatus('draft')} disabled={saveState === 'saving'}><FileEdit size={18} /> Voltar a rascunho</button>
          ) : (
            <button type="button" className="btn btn-primary" onClick={() => void changeStatus('published')} disabled={saveState === 'saving'}><Send size={18} /> Publicar</button>
          )}
        </div>
      </section>

      {error && <div className="alert alert-danger" role="alert">{error}</div>}
      {message && <div className="alert alert-success" role="status">{message}</div>}
      {validationErrors.length > 0 && (
        <div className="alert alert-warning" role="alert">
          <strong>Antes de publicar:</strong>
          <ul>{validationErrors.map((item) => <li key={item}>{item}</li>)}</ul>
        </div>
      )}

      <div className="editor-layout">
        <div className="editor-main-column">
          <section className="panel-card editor-section">
            <div className="section-heading"><div><h2>Informações principais</h2><p>Estes textos aparecem na listagem e na abertura do artigo.</p></div></div>
            <div className="form-field">
              <label htmlFor="article-title">Título</label>
              <input id="article-title" className="form-control form-control-lg" value={article.title} onChange={(event) => changeTitle(event.target.value)} maxLength={160} placeholder="Título claro e objetivo" />
              <small>{article.title.length}/160 caracteres</small>
            </div>
            <div className="form-field">
              <label htmlFor="article-subtitle">Subtítulo</label>
              <input id="article-subtitle" className="form-control" value={article.subtitle} onChange={(event) => patchArticle({ subtitle: event.target.value.slice(0, 240) })} maxLength={240} placeholder="Complemente o título com uma frase curta" />
              <small>{article.subtitle.length}/240 caracteres</small>
            </div>
            <div className="form-field">
              <label htmlFor="article-summary">Resumo</label>
              <textarea id="article-summary" className="form-control" rows={4} value={article.summary} onChange={(event) => patchArticle({ summary: event.target.value.slice(0, 400) })} maxLength={400} placeholder="Explique em poucas linhas o que a leitora encontrará" />
              <small>{article.summary.length}/400 caracteres</small>
            </div>
          </section>

          <section className="panel-card editor-section content-editor-section">
            <div className="section-heading"><div><h2>Conteúdo do artigo</h2><p>Use títulos, listas, destaque, links, citações e imagens por endereço.</p></div></div>
            <RichTextEditor content={article.content} onChange={(content) => patchArticle({ content })} />
          </section>
        </div>

        <aside className="editor-side-column">
          <section className="panel-card editor-section">
            <div className="section-heading"><div><h2>Publicação</h2><p>Status e histórico local.</p></div></div>
            <div className="publication-status-row">
              <span className={`status-badge ${article.status}`}>{article.status === 'published' ? 'Publicado' : 'Rascunho'}</span>
              <span>{article.status === 'published' ? formatDate(article.publishedAt) : 'Ainda não publicado'}</span>
            </div>
            <div className="publication-dates">
              <span><strong>Criado:</strong> {formatDate(article.createdAt)}</span>
              <span><strong>Atualizado:</strong> {formatDate(article.updatedAt)}</span>
            </div>
            <button type="button" className={`btn ${article.status === 'published' ? 'btn-secondary' : 'btn-primary'} w-100`} onClick={() => void changeStatus(article.status === 'published' ? 'draft' : 'published')} disabled={saveState === 'saving'}>
              {article.status === 'published' ? <FileEdit size={17} /> : <Send size={17} />}
              {article.status === 'published' ? 'Transformar em rascunho' : 'Publicar artigo'}
            </button>
          </section>

          <section className="panel-card editor-section">
            <div className="section-heading"><div><h2>Organização</h2><p>Dados usados em filtros e busca.</p></div></div>
            <div className="form-field">
              <label htmlFor="article-category">Categoria</label>
              <input id="article-category" className="form-control" list="article-categories" value={article.category} onChange={(event) => patchArticle({ category: event.target.value.slice(0, 80) })} maxLength={80} />
              <datalist id="article-categories">{suggestedCategories.map((item) => <option key={item} value={item} />)}</datalist>
            </div>
            <div className="form-field">
              <label htmlFor="article-author">Autor</label>
              <input id="article-author" className="form-control" value={article.author} onChange={(event) => patchArticle({ author: event.target.value.slice(0, 100) })} maxLength={100} />
            </div>
            <div className="form-field">
              <label htmlFor="article-slug">Endereço amigável</label>
              <input id="article-slug" className="form-control" value={article.slug} onChange={(event) => patchArticle({ slug: slugify(event.target.value) })} maxLength={120} placeholder="titulo-do-artigo" />
              <small>O painel evita endereços duplicados automaticamente.</small>
            </div>
            <div className="form-field">
              <label htmlFor="article-tags">Tags</label>
              <input id="article-tags" className="form-control" value={tagsText} onChange={(event) => {
                const value = event.target.value.slice(0, 500)
                setTagsText(value)
                patchArticle({ tags: parseTags(value) })
              }} placeholder="ciclo, prevenção, autocuidado" />
              <small>Separe por vírgulas. Máximo de 12 tags.</small>
            </div>
          </section>

          <section className="panel-card editor-section">
            <div className="section-heading"><div><h2>Imagem de capa</h2><p>Até 2 MB nesta versão local.</p></div></div>
            {article.coverImage ? (
              <div className="cover-editor-preview">
                <img src={article.coverImage} alt={article.coverAlt || 'Prévia da capa'} />
                <button type="button" className="cover-remove-button" aria-label="Remover imagem de capa" onClick={() => patchArticle({ coverImage: null, coverAlt: '' })}><Trash2 size={17} /></button>
              </div>
            ) : (
              <label className="cover-upload-placeholder" htmlFor="article-cover"><ImagePlus size={28} /><span>Selecionar imagem</span><small>PNG, JPEG, GIF ou WebP</small></label>
            )}
            <input id="article-cover" className="visually-hidden" type="file" accept="image/*" onChange={(event) => void handleCoverFile(event)} />
            {article.coverImage && <label className="btn btn-outline-primary w-100 mt-2" htmlFor="article-cover"><ImagePlus size={17} /> Trocar imagem</label>}
            <div className="form-field mt-3">
              <label htmlFor="article-cover-alt">Texto alternativo</label>
              <input id="article-cover-alt" className="form-control" value={article.coverAlt} onChange={(event) => patchArticle({ coverAlt: event.target.value.slice(0, 200) })} maxLength={200} placeholder="Descreva a imagem para acessibilidade" />
            </div>
          </section>
        </aside>
      </div>
    </div>
  )
}
