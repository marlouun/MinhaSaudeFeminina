import { useEffect, useState } from 'react'
import { articleRepository } from '../services'
import type { Article } from '../types/article'

export function useArticles() {
  const [articles, setArticles] = useState<Article[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    const unsubscribe = articleRepository.watchAll(
      (nextArticles) => {
        if (!active) return
        setArticles(nextArticles)
        setLoading(false)
        setError(null)
      },
      (reason) => {
        if (!active) return
        setError(reason instanceof Error ? reason.message : 'Não foi possível ler os artigos locais.')
        setLoading(false)
      },
    )

    void articleRepository.seedIfEmpty().catch((reason: unknown) => {
      if (!active) return
      setError(reason instanceof Error ? reason.message : 'Não foi possível criar os dados de demonstração.')
      setLoading(false)
    })

    return () => {
      active = false
      unsubscribe()
    }
  }, [])

  return { articles, loading, error }
}
