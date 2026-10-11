import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { CalendarPlus, Pencil, Plus, Search } from 'lucide-react'
import { api } from '../api'
import { useAsync } from '../useAsync'
import { Button, EmptyState, ErrorPanel, Field, Modal, PageHeader, Spinner, inputClass } from '../components/ui'
import { useToast } from '../components/Toast'
import type { Guest } from '../types'

const PHONE = /^\+?[0-9][0-9 -]{6,17}$/
const EMAIL = /^[^@\s]+@[^@\s]+\.[^@\s]+$/

function GuestForm({ guest, onClose, onSaved }: { guest: Guest | null; onClose: () => void; onSaved: () => void }) {
  const toast = useToast()
  const [name, setName] = useState(guest?.name ?? '')
  const [phone, setPhone] = useState(guest?.phone ?? '')
  const [email, setEmail] = useState(guest?.email ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [saving, setSaving] = useState(false)

  async function submit(ev: FormEvent) {
    ev.preventDefault()
    const e: Record<string, string> = {}
    if (name.trim().length < 2) e.name = 'Enter the guest name (at least 2 characters)'
    if (!PHONE.test(phone.trim())) e.phone = 'Enter a valid phone number (7 to 18 digits)'
    if (email.trim() && !EMAIL.test(email.trim())) e.email = 'Enter a valid e-mail or leave it blank'
    setErrors(e)
    if (Object.keys(e).length) return
    setSaving(true)
    try {
      const payload = { name: name.trim(), phone: phone.trim(), email: email.trim() }
      const saved = guest ? await api.updateGuest(guest.id, payload) : await api.createGuest(payload)
      toast.success(guest ? 'Guest details updated' : `Guest added with ID ${saved.guestCode}`)
      onSaved()
    } catch (err) {
      toast.error((err as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title={guest ? `Edit ${guest.guestCode}` : 'Add a guest'} onClose={onClose}>
      <form onSubmit={submit} className="space-y-4" noValidate>
        <Field label="Full name" htmlFor="gn" error={errors.name}>
          <input id="gn" className={inputClass} value={name} onChange={(e) => setName(e.target.value)} autoComplete="off" />
        </Field>
        <Field label="Phone number" htmlFor="gp" error={errors.phone}>
          <input id="gp" className={inputClass} value={phone} onChange={(e) => setPhone(e.target.value)} inputMode="tel" placeholder="e.g. 9000000001" />
        </Field>
        <Field label="E-mail (optional)" htmlFor="ge" error={errors.email}>
          <input id="ge" type="email" className={inputClass} value={email} onChange={(e) => setEmail(e.target.value)} />
        </Field>
        {!guest && <p className="text-xs text-muted">A unique guest ID (like G-1005) is generated automatically.</p>}
        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" loading={saving}>{guest ? 'Save changes' : 'Add guest'}</Button>
        </div>
      </form>
    </Modal>
  )
}

export default function Guests() {
  const [q, setQ] = useState('')
  const [debounced, setDebounced] = useState('')
  const [adding, setAdding] = useState(false)
  const [editing, setEditing] = useState<Guest | null>(null)

  useEffect(() => {
    const t = window.setTimeout(() => setDebounced(q), 250)
    return () => window.clearTimeout(t)
  }, [q])

  const { data: guests, loading, error, reload } = useAsync(() => api.guests(debounced), [debounced])

  return (
    <div>
      <PageHeader title="Guests" subtitle="Everyone who has stayed or booked with us."
        actions={<Button icon={<Plus className="h-4 w-4" />} onClick={() => setAdding(true)}>Add guest</Button>} />

      <div className="relative mb-6 max-w-md">
        <label htmlFor="gsearch" className="sr-only">Search guests</label>
        <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-muted" aria-hidden />
        <input id="gsearch" className={`${inputClass} pl-9`} placeholder="Search by name, phone, e-mail or guest ID" value={q} onChange={(e) => setQ(e.target.value)} />
      </div>

      {loading && !guests && <Spinner label="Loading guests" />}
      {error && <ErrorPanel message={error} onRetry={reload} />}
      {guests && guests.length === 0 && (
        <EmptyState title="No guests found" text={debounced ? 'No guest matches your search.' : 'Add your first guest to get started.'}
          action={!debounced ? <Button icon={<Plus className="h-4 w-4" />} onClick={() => setAdding(true)}>Add guest</Button> : undefined} />
      )}

      {guests && guests.length > 0 && (
        <div className="overflow-hidden rounded-xl border border-line bg-paper">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[640px] text-left text-sm">
              <thead className="bg-cream text-xs uppercase tracking-wider text-muted">
                <tr><th className="px-5 py-3 font-medium">Guest ID</th><th className="px-5 py-3 font-medium">Name</th><th className="px-5 py-3 font-medium">Phone</th><th className="px-5 py-3 font-medium">E-mail</th><th className="px-5 py-3"><span className="sr-only">Actions</span></th></tr>
              </thead>
              <tbody className="divide-y divide-line">
                {guests.map((g) => (
                  <tr key={g.id} className="transition-colors hover:bg-cream/60">
                    <td className="px-5 py-3.5 font-medium text-gold-deep">{g.guestCode}</td>
                    <td className="px-5 py-3.5 font-medium text-ink">{g.name}</td>
                    <td className="px-5 py-3.5 text-muted">{g.phone}</td>
                    <td className="px-5 py-3.5 text-muted">{g.email ?? '-'}</td>
                    <td className="px-5 py-3.5">
                      <div className="flex justify-end gap-2">
                        <button onClick={() => setEditing(g)} className="rounded-lg p-2 text-forest hover:bg-cream" aria-label={`Edit ${g.name}`}><Pencil className="h-4 w-4" /></button>
                        <Link to={`/bookings?new=1&guest=${g.id}`} className="rounded-lg p-2 text-forest hover:bg-cream" aria-label={`New booking for ${g.name}`}><CalendarPlus className="h-4 w-4" /></Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {(adding || editing) && (
        <GuestForm guest={editing} onClose={() => { setAdding(false); setEditing(null) }} onSaved={() => { setAdding(false); setEditing(null); reload() }} />
      )}
    </div>
  )
}
