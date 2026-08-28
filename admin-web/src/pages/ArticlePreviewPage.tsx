import { ArrowLeft, Edit3 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArticlePreview } from '../components/ArticlePreview'
import { LoadingScreen } from '../components/LoadingScreen'
import { articleRepository } from '../services'
import type { Article } from '../types/article'

export function ArticlePreviewPage() {
  const { articleId = '' } = useParams()
  const [article, setArticle] = useState<Article | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    setLoading(true)
    void articleRepository.getById(articleId)
      .then((result) => {
        if (!active) return
        if (!result) setError('Artigo não encontrado neste navegador.')
        else setArticle(result)
      })
      .catch((reason: unknown) => {
        if (active) setError(reason instanceof Error ? reason.message : 'Não foi possível abrir o artigo.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [articleId])

  if (loading) return <LoadingScreen label="Preparando visualização..." />

  return (
    <div className="page-stack preview-page">
      <section className="page-heading">
        <div>
          <span className="eyebrow">Pré-visualização</span>
          <h1>Como o artigo será apresentado</h1>
          <p>A estrutura abaixo usa o mesmo JSON que o aplicativo Android consegue interpretar.</p>
        </div>
        <div className="heading-actions">
          <Link className="btn btn-light" to="/articles"><ArrowLeft size={18} /> Artigos</Link>
          {article && <Link className="btn btn-primary" to={`/articles/${article.id}/edit`}><Edit3 size={18} /> Editar</Link>}
        </div>
      </section>
      {error && <div className="alert alert-danger" role="alert">{error}</div>}
      {article && <ArticlePreview article={article} />}
    </div>
  )
}
