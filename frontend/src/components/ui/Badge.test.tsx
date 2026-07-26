import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Badge } from './Badge'

describe('Badge', () => {
  it('renders children', () => {
    render(<Badge>ACTIVE</Badge>)
    expect(screen.getByText('ACTIVE')).toBeInTheDocument()
  })

  it('applies success tone classes', () => {
    render(<Badge tone="success">ON</Badge>)
    const el = screen.getByText('ON')
    expect(el.className).toContain('emerald')
  })
})
