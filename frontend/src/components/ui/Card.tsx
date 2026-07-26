import type { ReactNode } from 'react'

export function Card({
  children,
  className = '',
  title,
  action,
}: {
  children: ReactNode
  className?: string
  title?: string
  action?: ReactNode
}) {
  return (
    <section
      className={`rounded-2xl border border-border bg-panel/80 p-5 shadow-xl shadow-black/20 backdrop-blur ${className}`}
    >
      {(title || action) && (
        <div className="mb-4 flex items-center justify-between gap-3">
          {title && <h2 className="text-base font-semibold text-slate-100">{title}</h2>}
          {action}
        </div>
      )}
      {children}
    </section>
  )
}
