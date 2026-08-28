import { createRequire } from 'module'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

const __dirname = dirname(fileURLToPath(import.meta.url))
const require = createRequire(import.meta.url)
const sharp = require(join(__dirname, 'node_modules', 'sharp'))

const input  = join(__dirname, 'public', 'unifebe-logo.png')
const output = join(__dirname, 'public', 'unifebe-logo-transparent.png')

const { data, info } = await sharp(input)
  .ensureAlpha()
  .raw()
  .toBuffer({ resolveWithObject: true })

const { width, height, channels } = info
const pixels = new Uint8Array(data)

for (let i = 0; i < pixels.length; i += channels) {
  const r = pixels[i]
  const g = pixels[i + 1]
  const b = pixels[i + 2]

  // Detecta se o pixel é branco/cinza claro (fundo)
  const brightness = (r + g + b) / 3
  // Detecta saturação — pixels coloridos (logo rosa) têm alta diferença entre canais
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  const saturation = max === 0 ? 0 : (max - min) / max

  if (brightness > 200 && saturation < 0.15) {
    // pixel branco/cinza sem cor → totalmente transparente
    pixels[i + 3] = 0
  } else if (brightness > 160 && saturation < 0.12) {
    // cinza médio de borda → semitransparente (suaviza)
    const factor = (brightness - 160) / 40
    pixels[i + 3] = Math.round((1 - factor) * pixels[i + 3])
  }
}

await sharp(Buffer.from(pixels), { raw: { width, height, channels } })
  .png()
  .toFile(output)

console.log('✅ Salvo em public/unifebe-logo-transparent.png')
