import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Download, Printer } from 'lucide-react'
import { api } from '../api'
import { useAsync } from '../useAsync'
import { Badge, Button, ErrorPanel, Spinner } from '../components/ui'
import { useToast } from '../components/Toast'
import { formatDate, formatMoney } from '../format'

export default function InvoicePage() {
  const { id } = useParams()
  const toast = useToast()
  const { data: inv, loading, error, reload } = useAsync(() => api.invoice(id!), [id])
  const [downloading, setDownloading] = useState(false)

  async function download() {
    setDownloading(true)
    try {
      await api.downloadInvoice(id!)
      toast.success('Invoice downloaded')
    } catch (e) {
      toast.error((e as Error).message)
    } finally {
      setDownloading(false)
    }
  }

  if (loading && !inv) return <Spinner label="Preparing invoice" />
  if (error) return <ErrorPanel message={error} onRetry={reload} />
  if (!inv) return null

  return (
    <div>
      <div className="no-print mb-6 flex flex-wrap items-center justify-between gap-3">
        <Link to={`/bookings/${id}`} className="inline-flex items-center gap-2 text-sm font-medium text-forest hover:underline"><ArrowLeft className="h-4 w-4" /> Back to booking</Link>
        <div className="flex gap-3">
          <Button variant="secondary" icon={<Download className="h-4 w-4" />} loading={downloading} onClick={download}>Download .txt</Button>
          <Button icon={<Printer className="h-4 w-4" />} onClick={() => window.print()}>Print invoice</Button>
        </div>
      </div>

      <article className="print-sheet mx-auto max-w-3xl rounded-2xl border border-line bg-paper p-8 shadow-sm sm:p-12">
        <header className="flex flex-col gap-6 border-b-2 border-gold pb-8 sm:flex-row sm:justify-between">
          <div>
            <div className="font-display text-4xl font-semibold tracking-[0.2em] text-forest-dark">HOTELIER</div>
            <p className="mt-1 text-xs uppercase tracking-[0.3em] text-gold-deep">Boutique Hotel</p>
            <p className="mt-3 text-sm text-muted">12 Garden Road, Jaipur, Rajasthan<br />(fictional address for demonstration)</p>
          </div>
          <div className="sm:text-right">
            <h1 className="text-3xl font-semibold text-forest-dark">Invoice</h1>
            <p className="mt-1 text-sm font-medium">{inv.invoiceNumber}</p>
            <p className="text-sm text-muted">Issued {formatDate(inv.issuedOn)}</p>
            <div className="mt-2"><Badge value={inv.paymentStatus} label={inv.paymentStatus === 'PAID' ? 'Paid' : 'Payment pending'} /></div>
          </div>
        </header>

        <section className="grid gap-6 py-8 sm:grid-cols-2">
          <div>
            <h2 className="text-xs font-medium uppercase tracking-widest text-muted">Billed to</h2>
            <p className="mt-2 font-medium">{inv.guestName}</p>
            <p className="text-sm text-muted">Guest ID {inv.guestCode}<br />{inv.guestPhone}</p>
          </div>
          <div>
            <h2 className="text-xs font-medium uppercase tracking-widest text-muted">Stay</h2>
            <p className="mt-2 font-medium">Room {inv.roomNumber} · {inv.roomType}</p>
            <p className="text-sm text-muted">{formatDate(inv.checkInDate)} to {formatDate(inv.checkOutDate)}<br />Booking {inv.bookingReference}</p>
          </div>
        </section>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[480px] text-left text-sm">
            <thead>
              <tr className="border-y border-line bg-cream text-xs uppercase tracking-wider text-muted">
                <th className="px-3 py-3 font-medium">Description</th><th className="px-3 py-3 text-right font-medium">Nights</th><th className="px-3 py-3 text-right font-medium">Rate</th><th className="px-3 py-3 text-right font-medium">Amount</th>
              </tr>
            </thead>
            <tbody>
              {inv.lines.map((l, i) => (
                <tr key={i} className="border-b border-line">
                  <td className="px-3 py-4">{l.description}</td>
                  <td className="px-3 py-4 text-right">{l.quantity}</td>
                  <td className="px-3 py-4 text-right">{formatMoney(l.unitPrice)}</td>
                  <td className="px-3 py-4 text-right font-medium">{formatMoney(l.amount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <dl className="ml-auto mt-6 max-w-xs space-y-2 text-sm">
          <div className="flex justify-between"><dt className="text-muted">Subtotal</dt><dd>{formatMoney(inv.subtotal)}</dd></div>
          <div className="flex justify-between"><dt className="text-muted">GST ({inv.gstPercent}%)</dt><dd>{formatMoney(inv.tax)}</dd></div>
          <div className="flex items-baseline justify-between border-t-2 border-gold pt-3"><dt className="font-medium">Total</dt><dd className="font-display text-3xl font-semibold text-forest-dark">{formatMoney(inv.total)}</dd></div>
        </dl>

        <p className="mt-10 border-t border-line pt-6 text-center text-xs text-muted">Thank you for staying with HOTELIER. Payment status is simulated for this college project.</p>
      </article>
    </div>
  )
}
