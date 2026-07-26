import type { ReactNode } from 'react'

const tones: Record<string, string> = {
  success: 'bg-emerald-500/15 text-emerald-300 ring-emerald-500/30',
  danger: 'bg-rose-500/15 text-rose-300 ring-rose-500/30',
  warning: 'bg-amber-500/15 text-amber-300 ring-amber-500/30',
  info: 'bg-sky-500/15 text-sky-300 ring-sky-500/30',
  neutral: 'bg-slate-500/15 text-slate-300 ring-slate-500/30',
  brand: 'bg-indigo-500/15 text-indigo-300 ring-indigo-500/30',
}

export function Badge({
  children,
  tone = 'neutral',
}: {
  children: ReactNode
  tone?: keyof typeof tones
}) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${tones[tone]}`}
    >
      {children}
    </span>
  )
}
