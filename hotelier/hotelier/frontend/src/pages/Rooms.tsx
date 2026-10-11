import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { BedDouble, CalendarPlus, Pencil, Plus, Search, Users } from 'lucide-react'
import { api } from '../api'
import { useAsync } from '../useAsync'
import { Badge, Button, EmptyState, ErrorPanel, Field, Modal, PageHeader, Spinner, inputClass } from '../components/ui'
import { useToast } from '../components/Toast'
import { formatMoney } from '../format'
import type { Room } from '../types'

function RoomForm({ room, onClose, onSaved }: { room: Room | null; onClose: () => void; onSaved: () => void }) {
  const toast = useToast()
  const editing = room !== null
  const [number, setNumber] = useState(room?.roomNumber ?? '')
  const [type, setType] = useState(room ? room.type.toUpperCase() : 'STANDARD')
  const [capacity, setCapacity] = useState(String(room?.capacity ?? 2))
  const [rate, setRate] = useState(String(room?.baseRate ?? ''))
  const [status, setStatus] = useState(room?.status ?? 'AVAILABLE')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [saving, setSaving] = useState(false)

  function validate() {
    const e: Record<string, string> = {}
    if (!number.trim()) e.number = 'Room number is required'
    else if (number.trim().length > 10) e.number = 'Maximum 10 characters'
    const cap = Number(capacity)
    if (!Number.isInteger(cap) || cap < 1 || cap > 10) e.capacity = 'Enter a whole number from 1 to 10'
    const r = Number(rate)
    if (!rate || !(r > 0) || r > 1000000) e.rate = 'Enter a price above 0 (maximum 1,000,000)'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  async function submit(ev: FormEvent) {
    ev.preventDefault()
    if (!validate()) return
    setSaving(true)
    const payload = { roomNumber: number.trim(), type, capacity: Number(capacity), baseRate: Number(rate), status: editing && status !== 'OCCUPIED' ? status : undefined }
    try {
      if (editing) await api.updateRoom(room.id, payload)
      else await api.createRoom(payload)
      toast.success(editing ? `Room ${payload.roomNumber} updated` : `Room ${payload.roomNumber} added`)
      onSaved()
    } catch (e) {
      toast.error((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title={editing ? `Edit room ${room.roomNumber}` : 'Add a room'} onClose={onClose}>
      <form onSubmit={submit} className="space-y-4" noValidate>
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Room number" htmlFor="rn" error={errors.number}>
            <input id="rn" className={inputClass} value={number} onChange={(e) => setNumber(e.target.value)} placeholder="e.g. 105" />
          </Field>
          <Field label="Room type" htmlFor="rt" hint={editing ? 'The type cannot be changed after creation.' : 'Deluxe adds 15% and Suite adds 40% to the base price.'}>
            <select id="rt" className={inputClass} value={type} onChange={(e) => setType(e.target.value)} disabled={editing}>
              <option value="STANDARD">Standard</option>
              <option value="DELUXE">Deluxe</option>
              <option value="SUITE">Suite</option>
            </select>
          </Field>
          <Field label="Capacity (guests)" htmlFor="rc" error={errors.capacity}>
            <input id="rc" type="number" min={1} max={10} className={inputClass} value={capacity} onChange={(e) => setCapacity(e.target.value)} />
          </Field>
          <Field label="Base price per night (INR)" htmlFor="rp" error={errors.rate}>
            <input id="rp" type="number" min={1} className={inputClass} value={rate} onChange={(e) => setRate(e.target.value)} placeholder="e.g. 2500" />
          </Field>
        </div>
        {editing && (
          <Field label="Availability" htmlFor="rs" hint={room.status === 'OCCUPIED' ? 'This room is occupied. Check the guest out to release it.' : undefined}>
            <select id="rs" className={inputClass} value={status} onChange={(e) => setStatus(e.target.value as Room['status'])} disabled={room.status === 'OCCUPIED'}>
              {room.status === 'OCCUPIED' && <option value="OCCUPIED">Occupied</option>}
              <option value="AVAILABLE">Available</option>
              <option value="MAINTENANCE">Under maintenance</option>
            </select>
          </Field>
        )}
        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" loading={saving}>{editing ? 'Save changes' : 'Add room'}</Button>
        </div>
      </form>
    </Modal>
  )
}

export default function Rooms() {
  const [q, setQ] = useState('')
  const [debounced, setDebounced] = useState('')
  const [type, setType] = useState('')
  const [status, setStatus] = useState('')
  const [minCap, setMinCap] = useState('')
  const [editing, setEditing] = useState<Room | null>(null)
  const [adding, setAdding] = useState(false)

  useEffect(() => {
    const t = window.setTimeout(() => setDebounced(q), 250)
    return () => window.clearTimeout(t)
  }, [q])

  const { data: rooms, loading, error, reload } = useAsync(
    () => api.rooms({ q: debounced, type, status, minCapacity: minCap }),
    [debounced, type, status, minCap],
  )

  return (
    <div>
      <PageHeader title="Rooms" subtitle="Browse, search and manage every room in the hotel."
        actions={<Button icon={<Plus className="h-4 w-4" />} onClick={() => setAdding(true)}>Add room</Button>} />

      <div className="mb-6 grid gap-3 rounded-xl border border-line bg-paper p-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="relative">
          <label htmlFor="search" className="sr-only">Search by room number</label>
          <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-muted" aria-hidden />
          <input id="search" className={`${inputClass} pl-9`} placeholder="Search room number" value={q} onChange={(e) => setQ(e.target.value)} />
        </div>
        <div>
          <label htmlFor="ft" className="sr-only">Room type</label>
          <select id="ft" className={inputClass} value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">All types</option><option value="STANDARD">Standard</option><option value="DELUXE">Deluxe</option><option value="SUITE">Suite</option>
          </select>
        </div>
        <div>
          <label htmlFor="fs" className="sr-only">Status</label>
          <select id="fs" className={inputClass} value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">Any status</option><option value="AVAILABLE">Available</option><option value="OCCUPIED">Occupied</option><option value="MAINTENANCE">Maintenance</option>
          </select>
        </div>
        <div>
          <label htmlFor="fc" className="sr-only">Minimum capacity</label>
          <select id="fc" className={inputClass} value={minCap} onChange={(e) => setMinCap(e.target.value)}>
            <option value="">Any capacity</option><option value="2">2+ guests</option><option value="3">3+ guests</option><option value="4">4+ guests</option>
          </select>
        </div>
      </div>

      {loading && !rooms && <Spinner label="Loading rooms" />}
      {error && <ErrorPanel message={error} onRetry={reload} />}
      {rooms && rooms.length === 0 && <EmptyState title="No rooms found" text="Try changing the filters, or add a new room." />}

      {rooms && rooms.length > 0 && (
        <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
          {rooms.map((r, i) => (
            <article key={r.id} className="card-lift fade-up flex flex-col rounded-xl border border-line bg-paper p-5" style={{ animationDelay: `${Math.min(i, 8) * 40}ms` }}>
              <div className="flex items-start justify-between">
                <div>
                  <div className="text-xs uppercase tracking-[0.2em] text-gold-deep">{r.type}</div>
                  <h2 className="font-display text-5xl font-semibold leading-none text-forest-dark">{r.roomNumber}</h2>
                </div>
                <Badge value={r.status} />
              </div>
              <div className="mt-5 flex items-center gap-4 text-sm text-muted">
                <span className="flex items-center gap-1.5"><Users className="h-4 w-4" aria-hidden /> Up to {r.capacity}</span>
                <span className="flex items-center gap-1.5"><BedDouble className="h-4 w-4" aria-hidden /> {r.type}</span>
              </div>
              <div className="mt-4 border-t border-line pt-4">
                <span className="font-display text-3xl font-semibold text-forest">{formatMoney(r.nightlyRate)}</span>
                <span className="text-sm text-muted"> / night</span>
              </div>
              <div className="mt-4 flex gap-2">
                <Button variant="secondary" className="flex-1" icon={<Pencil className="h-4 w-4" />} onClick={() => setEditing(r)}>Edit</Button>
                {r.status !== 'MAINTENANCE' && (
                  <Link to={`/bookings?new=1&room=${r.id}`} className="flex-1">
                    <Button className="w-full" icon={<CalendarPlus className="h-4 w-4" />}>Book</Button>
                  </Link>
                )}
              </div>
            </article>
          ))}
        </div>
      )}

      {(adding || editing) && (
        <RoomForm room={editing} onClose={() => { setAdding(false); setEditing(null) }} onSaved={() => { setAdding(false); setEditing(null); reload() }} />
      )}
    </div>
  )
}
