import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { LoadingScreen } from '../components/LoadingScreen'
import { articleRepository } from '../services'
import type { Article } from '../types/article'
import { createEmptyArticle } from '../utils/article'

/**
 * Cria um único rascunho e redireciona para o editor.
 * A Promise é mantida em um ref para continuar correta mesmo quando o
 * React StrictMode repete o ciclo de efeitos no ambiente de desenvolvimento.
 */
export function NewArticlePage() {
  const navigate = useNavigate()
  const creationRef = useRef<Promise<Article> | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true

    creationRef.current ??= articleRepository.save(createEmptyArticle())
    void creationRef.current
      .then((article) => {
        if (active) navigate(`/articles/${article.id}/edit`, { replace: true })
      })
      .catch((reason: unknown) => {
        if (!active) return
        setError(reason instanceof Error ? reason.message : 'Não foi possível criar o rascunho.')
      })

    return () => {
      active = false
    }
  }, [navigate])

  if (!error) return <LoadingScreen label="Criando um novo rascunho..." />

  return (
    <div className="page-stack">
      <div className="alert alert-danger" role="alert">{error}</div>
      <Link className="btn btn-light align-self-start" to="/articles">Voltar aos artigos</Link>
    </div>
  )
}
