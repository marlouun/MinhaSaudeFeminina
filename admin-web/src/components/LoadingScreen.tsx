export function LoadingScreen({ label = 'Carregando...' }: { label?: string }) {
  return (
    <div className="loading-screen" role="status" aria-live="polite">
      <div className="spinner-border text-primary" aria-hidden="true" />
      <span>{label}</span>
    </div>
  )
}
