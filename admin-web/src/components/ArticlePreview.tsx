import { EditorContent, useEditor } from '@tiptap/react'
import { CalendarDays, Tag, UserRound } from 'lucide-react'
import { useMemo } from 'react'
import { createEditorExtensions } from '../editor/extensions'
import type { Article } from '../types/article'
import { formatDate } from '../utils/article'
import { sanitizeArticleDocument } from '../utils/document'

interface ArticlePreviewProps {
  article: Article
}

export function ArticlePreview({ article }: ArticlePreviewProps) {
  const sanitized = useMemo(() => sanitizeArticleDocument(article.content), [article.content])
  const contentKey = useMemo(() => JSON.stringify(sanitized), [sanitized])
  const editor = useEditor(
    {
      extensions: createEditorExtensions({ openLinks: true, placeholder: '' }),
      content: sanitized,
      editable: false,
      immediatelyRender: false,
    },
    [contentKey],
  )

  return (
    <article className="article-preview-card">
      {article.coverImage && (
        <img
          className="article-preview-cover"
          src={article.coverImage}
          alt={article.coverAlt || `Imagem de capa de ${article.title}`}
        />
      )}
      <div className="article-preview-body">
        <span className="article-preview-category">{article.category || 'Sem categoria'}</span>
        <h1>{article.title || 'Artigo sem título'}</h1>
        {article.subtitle && <p className="article-preview-subtitle">{article.subtitle}</p>}
        <div className="article-preview-meta">
          <span><UserRound size={15} /> {article.author || 'Autor não informado'}</span>
          <span><CalendarDays size={15} /> {formatDate(article.updatedAt)}</span>
        </div>
        {article.summary && <p className="article-preview-summary">{article.summary}</p>}
        <EditorContent editor={editor} className="article-preview-content" />
        {article.tags.length > 0 && (
          <div className="article-preview-tags" aria-label="Tags do artigo">
            <Tag size={16} />
            {article.tags.map((tag) => <span key={tag}>#{tag}</span>)}
          </div>
        )}
        <div className="article-health-note">
          Conteúdo informativo. Diagnóstico, tratamento e situações de urgência exigem avaliação de um serviço de saúde.
        </div>
      </div>
    </article>
  )
}
