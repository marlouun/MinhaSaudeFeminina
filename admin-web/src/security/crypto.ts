const encoder = new TextEncoder()
const DEFAULT_ITERATIONS = 180_000
const SALT_SIZE = 16
const HASH_SIZE_BITS = 256

export interface PasswordDigest {
  hash: string
  salt: string
  iterations: number
}

function bytesToBase64(bytes: Uint8Array): string {
  let binary = ''
  for (const byte of bytes) binary += String.fromCharCode(byte)
  return btoa(binary)
}

function base64ToBytes(value: string): Uint8Array {
  const binary = atob(value)
  return Uint8Array.from(binary, (character) => character.charCodeAt(0))
}

function toArrayBuffer(bytes: Uint8Array): ArrayBuffer {
  const copy = new Uint8Array(bytes.byteLength)
  copy.set(bytes)
  return copy.buffer
}

async function derivePassword(
  password: string,
  salt: Uint8Array,
  iterations: number,
): Promise<Uint8Array> {
  const keyMaterial = await crypto.subtle.importKey(
    'raw',
    toArrayBuffer(encoder.encode(password)),
    'PBKDF2',
    false,
    ['deriveBits'],
  )
  const bits = await crypto.subtle.deriveBits(
    {
      name: 'PBKDF2',
      hash: 'SHA-256',
      salt: toArrayBuffer(salt),
      iterations,
    },
    keyMaterial,
    HASH_SIZE_BITS,
  )
  return new Uint8Array(bits)
}

export async function createPasswordDigest(password: string): Promise<PasswordDigest> {
  const salt = crypto.getRandomValues(new Uint8Array(SALT_SIZE))
  const hash = await derivePassword(password, salt, DEFAULT_ITERATIONS)
  return {
    hash: bytesToBase64(hash),
    salt: bytesToBase64(salt),
    iterations: DEFAULT_ITERATIONS,
  }
}

export async function verifyPassword(
  password: string,
  expectedHash: string,
  encodedSalt: string,
  iterations: number,
): Promise<boolean> {
  try {
    const actual = await derivePassword(password, base64ToBytes(encodedSalt), iterations)
    const expected = base64ToBytes(expectedHash)
    if (actual.length !== expected.length) return false

    let difference = 0
    for (let index = 0; index < actual.length; index += 1) {
      difference |= actual[index]! ^ expected[index]!
    }
    return difference === 0
  } catch {
    return false
  }
}

export function normalizeEmail(email: string): string {
  return email.trim().toLocaleLowerCase('pt-BR')
}
