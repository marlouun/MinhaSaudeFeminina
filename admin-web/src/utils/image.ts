const MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024

const IMAGE_TYPES = new Set(['image/png', 'image/jpeg', 'image/gif', 'image/webp'])

export function validateImageFile(file: File): void {
  if (!IMAGE_TYPES.has(file.type)) {
    throw new Error('Selecione uma imagem PNG, JPEG, GIF ou WebP.')
  }
  if (file.size > MAX_IMAGE_SIZE_BYTES) {
    throw new Error('A imagem pode ter no máximo 2 MB.')
  }
}

export async function imageFileToDataUrl(file: File): Promise<string> {
  validateImageFile(file)
  return await new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(new Error('Não foi possível ler a imagem.'))
    reader.readAsDataURL(file)
  })
}
