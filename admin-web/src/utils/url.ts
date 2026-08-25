export function normalizeSafeHttpUrl(value: string): string | null {
  const trimmed = value.trim()
  if (!trimmed) return null

  const candidate = /^[a-z][a-z\d+.-]*:/i.test(trimmed) ? trimmed : `https://${trimmed}`
  try {
    const url = new URL(candidate)
    if (url.protocol !== 'https:' && url.protocol !== 'http:') return null
    if (!url.hostname) return null
    return url.toString()
  } catch {
    return null
  }
}

export function isSafeHttpUrl(value: string): boolean {
  return normalizeSafeHttpUrl(value) !== null
}
