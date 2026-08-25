import { useEffect } from 'react'
import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { LoadingScreen } from './components/LoadingScreen'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import { AdminLayout } from './layout/AdminLayout'
import { ArticleEditorPage } from './pages/ArticleEditorPage'
import { ArticlePreviewPage } from './pages/ArticlePreviewPage'
import { ArticlesPage } from './pages/ArticlesPage'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { articleRepository } from './services'

function ProtectedRoute() {
  const { loading, session } = useAuth()
  if (loading) return <LoadingScreen label="Validando a sessão local..." />
  return session ? <Outlet /> : <Navigate to="/login" replace />
}

function ApplicationRoutes() {
  useEffect(() => {
    void articleRepository.seedIfEmpty()
  }, [])

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AdminLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="articles" element={<ArticlesPage />} />
          <Route path="articles/new" element={<ArticleEditorPage />} />
          <Route path="articles/:articleId/edit" element={<ArticleEditorPage />} />
          <Route path="articles/:articleId/preview" element={<ArticlePreviewPage />} />
        </Route>
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ApplicationRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
