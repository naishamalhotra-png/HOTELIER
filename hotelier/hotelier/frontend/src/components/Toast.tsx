import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { CheckCircle2, XCircle } from 'lucide-react'

type ToastKind = 'success' | 'error'
interface ToastItem { id: number; kind: ToastKind; message: string }
interface ToastApi { success: (m: string) => void; error: (m: string) => void }

const ToastContext = createContext<ToastApi | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([])

  const push = useCallback((kind: ToastKind, message: string) => {
    const id = Date.now() + Math.random()
    setItems((list) => [...list, { id, kind, message }])
    window.setTimeout(() => setItems((list) => list.filter((t) => t.id !== id)), kind === 'error' ? 6000 : 3800)
  }, [])

  const api: ToastApi = { success: (m) => push('success', m), error: (m) => push('error', m) }

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className="no-print fixed right-4 top-4 z-[60] flex w-[min(92vw,380px)] flex-col gap-3" aria-live="polite">
        {items.map((t) => (
          <div
            key={t.id}
            role={t.kind === 'error' ? 'alert' : 'status'}
            className={`toast-enter flex items-start gap-3 rounded-xl border bg-paper p-4 shadow-lg ${t.kind === 'error' ? 'border-danger/40' : 'border-forest/30'}`}
          >
            {t.kind === 'error'
              ? <XCircle className="mt-0.5 h-5 w-5 shrink-0 text-danger" aria-hidden />
              : <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0 text-forest" aria-hidden />}
            <p className="text-sm text-ink">{t.message}</p>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast(): ToastApi {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast must be used inside ToastProvider')
  return ctx
}
