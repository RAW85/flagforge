import type { InputHTMLAttributes } from 'react'

export function Input({
  label,
  error,
  className = '',
  ...props
}: InputHTMLAttributes<HTMLInputElement> & {
  label?: string
  error?: string
}) {
  return (
    <label className="block space-y-1.5 text-left">
      {label && <span className="text-sm text-slate-300">{label}</span>}
      <input
        className={`w-full rounded-lg border border-border bg-panel px-3 py-2 text-sm text-slate-100 outline-none ring-indigo-500/40 placeholder:text-slate-500 focus:ring-2 ${className}`}
        {...props}
      />
      {error && <span className="text-xs text-rose-400">{error}</span>}
    </label>
  )
}
