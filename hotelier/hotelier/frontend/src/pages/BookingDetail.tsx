import { useState, type ReactNode } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Ban, CheckCircle2, CreditCard, LogIn, LogOut, Receipt } from 'lucide-react'
import { api } from '../api'
import { useAsync } from '../useAsync'
import { Badge, Button, Card, ConfirmDialog, ErrorPanel, Spinner } from '../components/ui'
import { useToast } from '../components/Toast'
import { formatDate, formatDateTime, formatMoney } from '../format'
import type { Booking } from '../types'

const steps: { key: Booking['status']; label: string }[] = [
  { key: 'CONFIRMED', label: 'Confirmed' },
  { key: 'CHECKED_IN', label: 'Checked in' },
  { key: 'CHECKED_OUT', label: 'Checked out' },
]

function Row({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-6 border-b border-line py-3 text-sm last:border-0">
      <dt className="text-muted">{label}</dt>
      <dd className="text-right font-medium text-ink">{children}</dd>
    </div>
  )
}

export default function BookingDetail() {
  const { id } = useParams()
  const toast = useToast()
  const { data: booking, loading, error, reload, setData } = useAsync(() => api.booking(id!), [id])
  const [busy, setBusy] = useState<string | null>(null)
  const [confirmCancel, setConfirmCancel] = useState(false)

  async function run(name: string, action: () => Promise<Booking>, success: string) {
    setBusy(name)
    try {
      setData(await action())
      toast.success(success)
    } catch (e) {
      toast.error((e as Error).message)
    } finally {
      setBusy(null)
      setConfirmCancel(false)
    }
  }

  if (loading && !booking) return <Spinner label="Loading booking" />
  if (error) return <ErrorPanel message={error} onRetry={reload} />
  if (!booking) return null

  const b = booking
  const stepIndex = steps.findIndex((s) => s.key === b.status)

  return (
    <div>
      <Link to="/bookings" className="mb-6 inline-flex items-center gap-2 text-sm font-medium text-forest hover:underline"><ArrowLeft className="h-4 w-4" /> All bookings</Link>

      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-xs uppercase tracking-[0.25em] text-gold-deep">Booking confirmation</p>
          <h1 className="text-5xl font-semibold text-forest-dark">{b.reference}</h1>
          <div className="mt-3 flex flex-wrap gap-2"><Badge value={b.status} /><Badge value={b.paymentStatus} label={b.paymentStatus === 'PAID' ? 'Paid' : 'Payment pending'} /></div>
        </div>
        <div className="flex flex-wrap gap-3">
          {b.status === 'CONFIRMED' && <Button icon={<LogIn className="h-4 w-4" />} loading={busy === 'in'} onClick={() => run('in', () => api.checkIn(b.id), 'Guest checked in')}>Check in</Button>}
          {b.status === 'CHECKED_IN' && <Button icon={<LogOut className="h-4 w-4" />} loading={busy === 'out'} onClick={() => run('out', () => api.checkOut(b.id), 'Guest checked out')}>Check out</Button>}
          {b.status !== 'CANCELLED' && b.paymentStatus === 'PENDING' && <Button variant="secondary" icon={<CreditCard className="h-4 w-4" />} loading={busy === 'pay'} onClick={() => run('pay', () => api.pay(b.id), 'Payment recorded (simulated)')}>Mark as paid</Button>}
          {b.status !== 'CANCELLED' && <Link to={`/bookings/${b.id}/invoice`}><Button variant="secondary" icon={<Receipt className="h-4 w-4" />}>View invoice</Button></Link>}
          {b.status === 'CONFIRMED' && <Button variant="ghost" className="!text-danger hover:!bg-danger-soft" icon={<Ban className="h-4 w-4" />} onClick={() => setConfirmCancel(true)}>Cancel booking</Button>}
        </div>
      </div>

      {b.status !== 'CANCELLED' ? (
        <ol className="mb-8 grid grid-cols-3 gap-2" aria-label="Booking progress">
          {steps.map((s, i) => (
            <li key={s.key} className={`rounded-lg border px-3 py-3 text-center text-sm ${i <= stepIndex ? 'border-forest bg-forest text-ivory' : 'border-line bg-paper text-muted'}`}>
              <span className="flex items-center justify-center gap-2">{i <= stepIndex && <CheckCircle2 className="h-4 w-4" aria-hidden />}{s.label}</span>
            </li>
          ))}
        </ol>
      ) : (
        <div className="mb-8 rounded-lg border border-danger/30 bg-danger-soft px-4 py-3 text-sm text-danger">This booking was cancelled. The room is free for these dates again.</div>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <Card className="p-6">
          <h2 className="mb-2 text-3xl font-semibold text-forest-dark">Stay details</h2>
          <dl>
            <Row label="Guest">{b.guest.name} <span className="text-xs font-normal text-muted">({b.guest.guestCode})</span></Row>
            <Row label="Contact">{b.guest.phone}</Row>
            <Row label="Room">{b.room.roomNumber} · {b.room.type}</Row>
            <Row label="Guests">{b.numberOfGuests}</Row>
            <Row label="Check-in">{formatDate(b.checkInDate)}</Row>
            <Row label="Check-out">{formatDate(b.checkOutDate)}</Row>
            <Row label="Nights">{b.nights}</Row>
            {b.checkedInAt && <Row label="Checked in at">{formatDateTime(b.checkedInAt)}</Row>}
            {b.checkedOutAt && <Row label="Checked out at">{formatDateTime(b.checkedOutAt)}</Row>}
          </dl>
        </Card>
        <Card className="p-6">
          <h2 className="mb-2 text-3xl font-semibold text-forest-dark">Price</h2>
          <dl>
            <Row label="Rate per night">{formatMoney(b.nightlyRate)}</Row>
            <Row label="Nights">× {b.nights}</Row>
            <Row label="Room total (before GST)"><span className="font-display text-2xl text-forest-dark">{formatMoney(b.totalPrice)}</span></Row>
          </dl>
          <p className="mt-4 text-xs text-muted">The total was calculated by the Java backend when the booking was created. The invoice adds 12% GST.</p>
        </Card>
      </div>

      {confirmCancel && (
        <ConfirmDialog title="Cancel this booking?" message={`Booking ${b.reference} for ${b.guest.name} will be cancelled. This cannot be undone.`}
          confirmLabel="Yes, cancel booking" loading={busy === 'cancel'} onClose={() => setConfirmCancel(false)}
          onConfirm={() => run('cancel', () => api.cancelBooking(b.id), 'Booking cancelled')} />
      )}
    </div>
  )
}
