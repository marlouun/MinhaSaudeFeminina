import { describe, expect, it } from 'vitest'
import { normalizeSafeHttpUrl } from './url'

describe('normalizeSafeHttpUrl', () => {
  it('adiciona https quando o protocolo não foi informado', () => {
    expect(normalizeSafeHttpUrl('example.com/artigo')).toBe('https://example.com/artigo')
  })

  it('aceita apenas http e https', () => {
    expect(normalizeSafeHttpUrl('https://example.com')).toBe('https://example.com/')
    expect(normalizeSafeHttpUrl('http://example.com')).toBe('http://example.com/')
    expect(normalizeSafeHttpUrl('javascript:alert(1)')).toBeNull()
    expect(normalizeSafeHttpUrl('data:text/html,teste')).toBeNull()
  })

  it('rejeita entradas inválidas', () => {
    expect(normalizeSafeHttpUrl('')).toBeNull()
    expect(normalizeSafeHttpUrl('https://')).toBeNull()
  })
})
