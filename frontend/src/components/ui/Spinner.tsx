export function Spinner({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="flex items-center gap-2 text-sm text-slate-400">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-indigo-400 border-t-transparent" />
      {label}
    </div>
  )
}
