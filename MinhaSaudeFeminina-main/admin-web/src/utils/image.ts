const MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024

export async function imageFileToDataUrl(file: File): Promise<string> {
  if (!file.type.startsWith('image/')) {
    throw new Error('Selecione um arquivo de imagem.')
  }
  if (file.size > MAX_IMAGE_SIZE_BYTES) {
    throw new Error('A imagem pode ter no máximo 2 MB nesta versão local.')
  }

  return await new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(new Error('Não foi possível ler a imagem.'))
    reader.readAsDataURL(file)
  })
}
