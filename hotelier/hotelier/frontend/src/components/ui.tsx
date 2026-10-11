import { useEffect, useRef, type ReactNode, type ButtonHTMLAttributes } from 'react'
import { AlertTriangle, Loader2, X } from 'lucide-react'
import { statusLabel } from '../format'

/* ---------- Button ---------- */
type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'
const variants: Record<Variant, string> = {
  primary: 'bg-forest text-ivory hover:bg-forest-light shadow-sm',
  secondary: 'bg-paper text-forest border border-line hover:border-gold hover:bg-cream',
  ghost: 'text-forest hover:bg-cream',
  danger: 'bg-danger text-white hover:bg-[#82291f]',
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  loading?: boolean
  icon?: ReactNode
}

export function Button({ variant = 'primary', loading, icon, children, className = '', disabled, ...rest }: ButtonProps) {
  return (
    <button
      {...rest}
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-55 ${variants[variant]} ${className}`}
    >
      {loading ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden /> : icon}
      {children}
    </button>
  )
}

/* ---------- Badge ---------- */
const tones: Record<string, string> = {
  AVAILABLE: 'bg-[#dcebe2] text-forest-dark',
  OCCUPIED: 'bg-gold-soft text-gold-deep',
  MAINTENANCE: 'bg-[#e6e3da] text-[#4a4f4b]',
  CONFIRMED: 'bg-[#d9e9e7] text-teal-deep',
  CHECKED_IN: 'bg-[#dcebe2] text-forest-dark',
  CHECKED_OUT: 'bg-[#e6e3da] text-[#4a4f4b]',
  CANCELLED: 'bg-danger-soft text-danger',
  PAID: 'bg-[#dcebe2] text-forest-dark',
  PENDING: 'bg-warn-soft text-gold-deep',
}

export function Badge({ value, label }: { value: string; label?: string }) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${tones[value] ?? 'bg-cream text-ink'}`}>
      {label ?? statusLabel(value)}
    </span>
  )
}

/* ---------- Form field ---------- */
export const inputClass =
  'w-full rounded-lg border border-line bg-paper px-3 py-2.5 text-sm text-ink placeholder:text-[#8a938c] focus:border-teal-deep disabled:bg-cream disabled:text-muted'

export function Field({ label, error, hint, htmlFor, children }: { label: string; error?: string; hint?: string; htmlFor: string; children: ReactNode }) {
  return (
    <div>
      <label htmlFor={htmlFor} className="mb-1.5 block text-sm font-medium text-ink">{label}</label>
      {children}
      {hint && !error && <p className="mt-1 text-xs text-muted">{hint}</p>}
      {error && <p className="mt-1 text-xs font-medium text-danger" role="alert">{error}</p>}
    </div>
  )
}

/* ---------- Modal ---------- */
export function Modal({ title, onClose, children, wide }: { title: string; onClose: () => void; children: ReactNode; wide?: boolean }) {
  const ref = useRef<HTMLDivElement>(null)
  useEffect(() => {
    const previous = document.activeElement as HTMLElement | null
    ref.current?.focus()
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', onKey)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.style.overflow = ''
      previous?.focus()
    }
  }, [onClose])

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center sm:p-4">
      <div className="backdrop-enter absolute inset-0 bg-forest-dark/55" onClick={onClose} aria-hidden />
      <div
        ref={ref}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className={`modal-enter relative max-h-[92vh] w-full overflow-y-auto rounded-t-2xl bg-paper shadow-2xl sm:rounded-2xl ${wide ? 'sm:max-w-2xl' : 'sm:max-w-lg'}`}
      >
        <div className="sticky top-0 z-10 flex items-center justify-between border-b border-line bg-paper px-6 py-4">
          <h2 className="text-2xl font-semibold text-forest-dark">{title}</h2>
          <button onClick={onClose} aria-label="Close dialog" className="rounded-full p-1.5 text-muted hover:bg-cream">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="px-6 py-5">{children}</div>
      </div>
    </div>
  )
}

export function ConfirmDialog({ title, message, confirmLabel, loading, onConfirm, onClose }: {
  title: string; message: string; confirmLabel: string; loading?: boolean; onConfirm: () => void; onClose: () => void
}) {
  return (
    <Modal title={title} onClose={onClose}>
      <div className="flex gap-3">
        <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0 text-danger" aria-hidden />
        <p className="text-sm text-ink">{message}</p>
      </div>
      <div className="mt-6 flex justify-end gap-3">
        <Button variant="secondary" onClick={onClose}>Keep it</Button>
        <Button variant="danger" loading={loading} onClick={onConfirm}>{confirmLabel}</Button>
      </div>
    </Modal>
  )
}

/* ---------- misc ---------- */
export function PageHeader({ title, subtitle, actions }: { title: string; subtitle?: string; actions?: ReactNode }) {
  return (
    <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-4xl font-semibold text-forest-dark">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-muted">{subtitle}</p>}
        <div className="mt-3 h-px w-16 bg-gold" aria-hidden />
      </div>
      {actions && <div className="flex flex-wrap gap-3">{actions}</div>}
    </div>
  )
}

export function Spinner({ label = 'Loading' }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-3 py-16 text-muted" role="status">
      <Loader2 className="h-5 w-5 animate-spin" aria-hidden />
      <span className="text-sm">{label}...</span>
    </div>
  )
}

export function ErrorPanel({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="rounded-xl border border-danger/30 bg-danger-soft p-6 text-center" role="alert">
      <p className="text-sm font-medium text-danger">{message}</p>
      {onRetry && <Button variant="secondary" className="mt-4" onClick={onRetry}>Try again</Button>}
    </div>
  )
}

export function EmptyState({ title, text, action }: { title: string; text: string; action?: ReactNode }) {
  return (
    <div className="rounded-xl border border-dashed border-gold/60 bg-paper px-6 py-14 text-center">
      <h3 className="text-2xl font-semibold text-forest-dark">{title}</h3>
      <p className="mx-auto mt-1 max-w-md text-sm text-muted">{text}</p>
      {action && <div className="mt-5">{action}</div>}
    </div>
  )
}

export function Card({ children, className = '' }: { children: ReactNode; className?: string }) {
  return <div className={`rounded-xl border border-line bg-paper ${className}`}>{children}</div>
}
