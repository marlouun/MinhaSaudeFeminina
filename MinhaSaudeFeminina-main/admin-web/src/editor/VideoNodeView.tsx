import { NodeViewWrapper } from '@tiptap/react'
import type { NodeViewProps } from '@tiptap/react'

export function VideoNodeView({ node, selected }: NodeViewProps) {
  const { src, title } = node.attrs as { src: string; title?: string }

  return (
    <NodeViewWrapper>
      <div
        className={`video-node-wrapper${selected ? ' video-node-selected' : ''}`}
        contentEditable={false}
      >
        <video
          src={src}
          title={title ?? undefined}
          controls
          controlsList="nodownload"
          playsInline
          className="video-node-player"
        >
          Seu navegador não suporta reprodução de vídeo.
        </video>
        {title && (
          <p className="video-node-caption">{title}</p>
        )}
      </div>
    </NodeViewWrapper>
  )
}
