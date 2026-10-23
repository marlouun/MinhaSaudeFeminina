import { beforeEach, describe, expect, it, vi } from 'vitest'
import { uploadPublicStorageObject } from '../supabase/client'
import { uploadArticleImage } from './articleImages'

vi.mock('../supabase/client', () => ({ uploadPublicStorageObject: vi.fn() }))
const upload = vi.mocked(uploadPublicStorageObject)

beforeEach(() => { upload.mockReset() })

describe('upload de imagens do conteúdo', () => {
  it.each(['image/png', 'image/jpeg', 'image/gif', 'image/webp'])('envia %s e retorna o endereço persistente', async (type) => {
    const file = new File(['image'], 'foto', { type })
    upload.mockResolvedValue('https://example.com/imagem')
    expect(await uploadArticleImage('artigo-1', file)).toBe('https://example.com/imagem')
    expect(upload).toHaveBeenCalledWith('article-images', expect.stringMatching(/^content\/artigo-1\/[a-f0-9-]+\.(png|jpg|gif|webp)$/), file)
  })

  it('recusa SVG e arquivos que não são imagens antes do envio', async () => {
    for (const type of ['image/svg+xml', 'text/html', 'application/pdf']) {
      await expect(uploadArticleImage('id', new File(['x'], 'arquivo', { type }))).rejects.toThrow('PNG, JPEG, GIF ou WebP')
    }
    expect(upload).not.toHaveBeenCalled()
  })

  it('recusa mais de 2 MB antes do envio', async () => {
    const file = new File([new Uint8Array(2 * 1024 * 1024 + 1)], 'grande.png', { type: 'image/png' })
    await expect(uploadArticleImage('id', file)).rejects.toThrow('2 MB')
    expect(upload).not.toHaveBeenCalled()
  })

  it('propaga falha de sessão ou rede sem retornar endereço inexistente', async () => {
    upload.mockRejectedValue(new Error('Sua sessão administrativa expirou. Entre novamente.'))
    await expect(uploadArticleImage('id', new File(['x'], 'foto.png', { type: 'image/png' }))).rejects.toThrow('sessão administrativa expirou')
  })
})
