import type { ButtonHTMLAttributes, ReactNode } from 'react'

const variants = {
  primary:
    'bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-900/30 disabled:bg-indigo-900',
  secondary:
    'bg-panel-2 hover:bg-slate-700 text-slate-100 ring-1 ring-border disabled:opacity-50',
  danger:
    'bg-rose-600/90 hover:bg-rose-500 text-white disabled:opacity-50',
  ghost:
    'bg-transparent hover:bg-white/5 text-slate-300 disabled:opacity-50',
}

export function Button({
  children,
  variant = 'primary',
  className = '',
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode
  variant?: keyof typeof variants
}) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-lg px-3.5 py-2 text-sm font-medium transition disabled:cursor-not-allowed ${variants[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}
