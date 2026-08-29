import { ImagePlus, Upload, Video, X } from 'lucide-react'
import { type ChangeEvent, useEffect, useRef, useState } from 'react'
import { imageFileToDataUrl } from '../utils/image'
import { normalizeSafeHttpUrl } from '../utils/url'

type MediaTab = 'image' | 'video'
type ImageMode = 'url' | 'file'

const MAX_VIDEO_SIZE_BYTES = 100 * 1024 * 1024 // 100 MB
const ACCEPTED_VIDEO_TYPES = ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime']
const ACCEPTED_VIDEO_EXTENSIONS = '.mp4, .webm, .ogg, .mov'

interface MediaModalProps {
  isOpen: boolean
  onClose(): void
  onInsertImage(url: string, alt: string): void
  onInsertVideo(src: string, title: string): void
  onUploadImage?(file: File): Promise<string>
}

function videoFileToDataUrl(file: File): Promise<string> {
  if (!ACCEPTED_VIDEO_TYPES.includes(file.type) && !file.name.match(/\.(mp4|webm|ogg|mov)$/i)) {
    throw new Error('Formato não suportado. Use MP4, WebM, OGG ou MOV.')
  }
  if (file.size > MAX_VIDEO_SIZE_BYTES) {
    throw new Error('O vídeo pode ter no máximo 100 MB.')
  }
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(new Error('Não foi possível ler o vídeo.'))
    reader.readAsDataURL(file)
  })
}

export function MediaModal({
  isOpen,
  onClose,
  onInsertImage,
  onInsertVideo,
  onUploadImage,
}: MediaModalProps) {
  const [activeTab, setActiveTab] = useState<MediaTab>('image')
  const [imageMode, setImageMode] = useState<ImageMode>('url')

  // image
  const [imageUrl, setImageUrl] = useState('')
  const [imageAlt, setImageAlt] = useState('')

  // video
  const [videoFile, setVideoFile] = useState<File | null>(null)
  const [videoPreview, setVideoPreview] = useState<string | null>(null)
  const [videoTitle, setVideoTitle] = useState('')

  const [isProcessing, setIsProcessing] = useState(false)
  const [progress, setProgress] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const imageFileRef = useRef<HTMLInputElement>(null)
  const videoFileRef = useRef<HTMLInputElement>(null)

  // reset ao abrir
  useEffect(() => {
    if (isOpen) {
      setActiveTab('image')
      setImageMode('url')
      setImageUrl('')
      setImageAlt('')
      setVideoFile(null)
      setVideoPreview(null)
      setVideoTitle('')
      setIsProcessing(false)
      setProgress(null)
      setError(null)
    }
  }, [isOpen])

  // limpar object URL ao desmontar ou trocar de arquivo
  useEffect(() => {
    return () => {
      if (videoPreview?.startsWith('blob:')) URL.revokeObjectURL(videoPreview)
    }
  }, [videoPreview])

  // ESC fecha o modal
  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape' && isOpen) onClose() }
    document.addEventListener('keydown', handler)
    return () => document.removeEventListener('keydown', handler)
  }, [isOpen, onClose])

  const handleBackdropClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget) onClose()
  }

  // ── imagem por arquivo ──────────────────────────────────────────────────
  const handleImageFileSelect = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setError(null)
    setIsProcessing(true)
    setProgress('Processando imagem...')
    try {
      const url = onUploadImage ? await onUploadImage(file) : await imageFileToDataUrl(file)
      onInsertImage(url, imageAlt)
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não foi possível processar a imagem.')
    } finally {
      setIsProcessing(false)
      setProgress(null)
    }
  }

  // ── vídeo por arquivo ───────────────────────────────────────────────────
  const handleVideoFileSelect = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setError(null)

    if (!ACCEPTED_VIDEO_TYPES.includes(file.type) && !file.name.match(/\.(mp4|webm|ogg|mov)$/i)) {
      setError('Formato não suportado. Use MP4, WebM, OGG ou MOV.')
      return
    }
    if (file.size > MAX_VIDEO_SIZE_BYTES) {
      setError('O vídeo pode ter no máximo 100 MB.')
      return
    }

    // criar blob URL para preview imediato sem ler o arquivo inteiro
    if (videoPreview?.startsWith('blob:')) URL.revokeObjectURL(videoPreview)
    setVideoPreview(URL.createObjectURL(file))
    setVideoFile(file)
    if (!videoTitle) setVideoTitle(file.name.replace(/\.[^.]+$/, ''))
  }

  const handleInsertVideo = async () => {
    if (!videoFile) return
    setError(null)
    setIsProcessing(true)
    setProgress('Lendo vídeo... (pode demorar alguns segundos)')
    try {
      const dataUrl = await videoFileToDataUrl(videoFile)
      onInsertVideo(dataUrl, videoTitle)
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não foi possível processar o vídeo.')
    } finally {
      setIsProcessing(false)
      setProgress(null)
    }
  }

  // ── imagem por URL ──────────────────────────────────────────────────────
  const handleInsertImageUrl = () => {
    setError(null)
    const trimmed = imageUrl.trim()
    if (!trimmed) { setError('Digite o endereço da imagem.'); return }
    const safe = normalizeSafeHttpUrl(trimmed)
    if (!safe) { setError('Use um endereço HTTP ou HTTPS válido.'); return }
    onInsertImage(safe, imageAlt)
    onClose()
  }

  if (!isOpen) return null

  const canInsertImage = imageMode === 'url'
    ? imageUrl.trim().length > 0
    : false // arquivo já insere direto ao selecionar

  const canInsertVideo = !!videoFile && !isProcessing

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick}>
      <div className="media-modal" role="dialog" aria-labelledby="media-modal-title" aria-modal="true">

        {/* Header */}
        <div className="modal-header">
          <h2 id="media-modal-title">Adicionar mídia</h2>
          <button type="button" className="modal-close-button" onClick={onClose}
            aria-label="Fechar" disabled={isProcessing}>
            <X size={20} />
          </button>
        </div>

        {/* Tabs */}
        <div className="modal-tabs">
          <button type="button"
            className={`modal-tab ${activeTab === 'image' ? 'active' : ''}`}
            onClick={() => { setActiveTab('image'); setError(null) }}
            disabled={isProcessing}>
            <ImagePlus size={18} /> Imagem
          </button>
          <button type="button"
            className={`modal-tab ${activeTab === 'video' ? 'active' : ''}`}
            onClick={() => { setActiveTab('video'); setError(null) }}
            disabled={isProcessing}>
            <Video size={18} /> Vídeo
          </button>
        </div>

        {/* Body */}
        <div className="modal-body">
          {error && <div className="alert alert-danger mb-3" role="alert">{error}</div>}
          {progress && (
            <div className="upload-progress-info">
              <div className="upload-spinner" />
              {progress}
            </div>
          )}

          {/* ── ABA IMAGEM ── */}
          {activeTab === 'image' && (
            <>
              <div className="upload-mode-toggle">
                <button type="button"
                  className={`mode-button ${imageMode === 'url' ? 'active' : ''}`}
                  onClick={() => setImageMode('url')} disabled={isProcessing}>
                  <Upload size={16} /> URL da imagem
                </button>
                <button type="button"
                  className={`mode-button ${imageMode === 'file' ? 'active' : ''}`}
                  onClick={() => setImageMode('file')} disabled={isProcessing}>
                  <Upload size={16} /> Fazer upload
                </button>
              </div>

              {imageMode === 'url' ? (
                <>
                  <div className="form-field">
                    <label htmlFor="img-url">Endereço da imagem</label>
                    <input id="img-url" type="url" className="form-control"
                      value={imageUrl} onChange={(e) => setImageUrl(e.target.value)}
                      placeholder="https://exemplo.com/imagem.jpg" disabled={isProcessing} />
                  </div>
                  <div className="form-field">
                    <label htmlFor="img-alt">Texto alternativo</label>
                    <input id="img-alt" type="text" className="form-control"
                      value={imageAlt} onChange={(e) => setImageAlt(e.target.value.slice(0, 200))}
                      placeholder="Descreva a imagem para acessibilidade"
                      maxLength={200} disabled={isProcessing} />
                    <small>{imageAlt.length}/200 caracteres</small>
                  </div>
                </>
              ) : (
                <>
                  <div className="file-upload-area" onClick={() => !isProcessing && imageFileRef.current?.click()}>
                    <Upload size={28} className="upload-area-icon" />
                    <span className="upload-area-label">
                      {isProcessing ? 'Processando...' : 'Clique para selecionar uma imagem'}
                    </span>
                    <small>PNG, JPEG, GIF ou WebP · até 2 MB</small>
                    <input ref={imageFileRef} type="file" className="visually-hidden"
                      accept="image/*" disabled={isProcessing}
                      onChange={(e) => void handleImageFileSelect(e)} />
                  </div>
                  <div className="form-field mt-3">
                    <label htmlFor="img-alt-upload">Texto alternativo</label>
                    <input id="img-alt-upload" type="text" className="form-control"
                      value={imageAlt} onChange={(e) => setImageAlt(e.target.value.slice(0, 200))}
                      placeholder="Descreva a imagem para acessibilidade"
                      maxLength={200} disabled={isProcessing} />
                    <small>{imageAlt.length}/200 caracteres</small>
                  </div>
                </>
              )}
            </>
          )}

          {/* ── ABA VÍDEO ── */}
          {activeTab === 'video' && (
            <>
              {/* Preview do vídeo selecionado */}
              {videoPreview ? (
                <div className="video-preview-wrapper">
                  <video src={videoPreview} controls className="video-preview" />
                  <button type="button" className="video-change-btn"
                    onClick={() => videoFileRef.current?.click()} disabled={isProcessing}>
                    <Upload size={15} /> Trocar vídeo
                  </button>
                </div>
              ) : (
                <div className="file-upload-area video-upload-area"
                  onClick={() => !isProcessing && videoFileRef.current?.click()}>
                  <Video size={36} className="upload-area-icon" />
                  <span className="upload-area-label">
                    Clique para selecionar um vídeo do seu computador
                  </span>
                  <small>MP4, WebM, OGG ou MOV · até 100 MB</small>
                </div>
              )}

              <input ref={videoFileRef} type="file" className="visually-hidden"
                accept={`video/mp4,video/webm,video/ogg,video/quicktime,${ACCEPTED_VIDEO_EXTENSIONS}`}
                disabled={isProcessing}
                onChange={handleVideoFileSelect} />

              {videoFile && (
                <>
                  <div className="form-field mt-3">
                    <label htmlFor="video-title">Título do vídeo</label>
                    <input id="video-title" type="text" className="form-control"
                      value={videoTitle} onChange={(e) => setVideoTitle(e.target.value.slice(0, 200))}
                      placeholder="Título ou descrição do vídeo"
                      maxLength={200} disabled={isProcessing} />
                  </div>
                  <div className="video-file-info">
                    <Video size={14} />
                    <span>{videoFile.name}</span>
                    <span className="video-file-size">
                      ({(videoFile.size / (1024 * 1024)).toFixed(1)} MB)
                    </span>
                  </div>
                </>
              )}
            </>
          )}
        </div>

        {/* Footer */}
        <div className="modal-footer">
          <button type="button" className="btn btn-light" onClick={onClose} disabled={isProcessing}>
            Cancelar
          </button>

          {activeTab === 'image' && imageMode === 'url' && (
            <button type="button" className="btn btn-primary"
              onClick={handleInsertImageUrl}
              disabled={isProcessing || !canInsertImage}>
              Inserir imagem
            </button>
          )}

          {activeTab === 'video' && (
            <button type="button" className="btn btn-primary"
              onClick={() => void handleInsertVideo()}
              disabled={!canInsertVideo}>
              {isProcessing ? 'Processando...' : 'Inserir vídeo'}
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
