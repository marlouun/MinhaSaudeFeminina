import { ArrowLeft, FileQuestion } from 'lucide-react'
import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <main className="not-found-page">
      <FileQuestion size={58} />
      <span className="eyebrow">Erro 404</span>
      <h1>Página não encontrada</h1>
      <p>O endereço acessado não existe neste painel.</p>
      <Link className="btn btn-primary" to="/"><ArrowLeft size={18} /> Voltar ao dashboard</Link>
    </main>
  )
}
