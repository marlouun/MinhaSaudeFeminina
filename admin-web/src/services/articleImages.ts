import { uploadPublicStorageObject } from '../supabase/client'
import { validateImageFile } from '../utils/image'

const extensions: Record<string, string> = {
  'image/png': 'png',
  'image/jpeg': 'jpg',
  'image/gif': 'gif',
  'image/webp': 'webp',
}

export async function uploadArticleImage(articleId: string, file: File): Promise<string> {
  validateImageFile(file)
  const path = `content/${encodeURIComponent(articleId)}/${crypto.randomUUID()}.${extensions[file.type]}`
  return await uploadPublicStorageObject('article-images', path, file)
}
