import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { Plus, Search } from 'lucide-react'
import { api } from '../api'
import { useAsync } from '../useAsync'
import { Badge, Button, EmptyState, ErrorPanel, Field, Modal, PageHeader, Spinner, inputClass } from '../components/ui'
import { useToast } from '../components/Toast'
import { addDaysISO, formatDate, formatMoney, nightsBetween, todayISO } from '../format'
import type { Room } from '../types'

function NewBookingModal({ presetRoom, presetGuest, onClose }: { presetRoom?: number; presetGuest?: number; onClose: () => void }) {
  const toast = useToast()
  const navigate = useNavigate()
  const today = todayISO()
  const { data: guests, loading: guestsLoading, error: guestsError } = useAsync(() => api.guests(), [])

  const [guestId, setGuestId] = useState(presetGuest ? String(presetGuest) : '')
  const [checkIn, setCheckIn] = useState(today)
  const [checkOut, setCheckOut] = useState(addDaysISO(today, 1))
  const [people, setPeople] = useState('1')
  const [roomId, setRoomId] = useState<number | null>(presetRoom ?? null)
  const [rooms, setRooms] = useState<Room[] | null>(null)
  const [roomsError, setRoomsError] = useState<string | null>(null)
  const [roomsLoading, setRoomsLoading] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [saving, setSaving] = useState(false)

  const nights = checkIn && checkOut ? nightsBetween(checkIn, checkOut) : 0
  const peopleNum = Number(people)

  const dateError =
    !checkIn || !checkOut ? 'Choose both dates'
    : checkIn < today ? 'Check-in cannot be in the past'
    : nights < 1 ? 'Check-out must be after check-in'
    : nights > 30 ? 'A stay cannot be longer than 30 nights'
    : ''

  // Ask the Java backend which rooms are really free for these dates.
  useEffect(() => {
    if (dateError || !Number.isInteger(peopleNum) || peopleNum < 1) { setRooms(null); return }
    let cancelled = false
    setRoomsLoading(true)
    setRoomsError(null)
    api.availableRooms(checkIn, checkOut, peopleNum)
      .then((list) => {
        if (cancelled) return
        setRooms(list)
        setRoomId((current) => (current !== null && list.some((r) => r.id === current) ? current : null))
      })
      .catch((e: Error) => { if (!cancelled) setRoomsError(e.message) })
      .finally(() => { if (!cancelled) setRoomsLoading(false) })
    return () => { cancelled = true }
  }, [checkIn, checkOut, peopleNum, dateError])

  const selectedRoom = rooms?.find((r) => r.id === roomId) ?? null

  async function submit(ev: FormEvent) {
    ev.preventDefault()
    const e: Record<string, string> = {}
    if (!guestId) e.guest = 'Select a guest'
    if (dateError) e.dates = dateError
    if (!Number.isInteger(peopleNum) || peopleNum < 1) e.people = 'Enter at least 1 guest'
    if (!roomId) e.room = 'Select an available room'
    setErrors(e)
    if (Object.keys(e).length) return
    setSaving(true)
    try {
      const booking = await api.createBooking({ guestId: Number(guestId), roomId: roomId!, checkInDate: checkIn, checkOutDate: checkOut, numberOfGuests: peopleNum })
      toast.success(`Booking ${booking.reference} confirmed`)
      navigate(`/bookings/${booking.id}`)
    } catch (err) {
      toast.error((err as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title="New booking" onClose={onClose} wide>
      <form onSubmit={submit} className="space-y-5" noValidate>
        <Field label="Guest" htmlFor="bg" error={errors.guest ?? guestsError ?? undefined}>
          <select id="bg" className={inputClass} value={guestId} onChange={(e) => setGuestId(e.target.value)} disabled={guestsLoading}>
            <option value="">{guestsLoading ? 'Loading guests...' : 'Select a guest'}</option>
            {guests?.map((g) => <option key={g.id} value={g.id}>{g.name} ({g.guestCode})</option>)}
          </select>
          {guests && guests.length === 0 && <p className="mt-1 text-xs text-muted">No guests yet. <Link to="/guests" className="underline">Add a guest first</Link>.</p>}
        </Field>

        <div className="grid gap-4 sm:grid-cols-3">
          <Field label="Check-in" htmlFor="bci" error={errors.dates}>
            <input id="bci" type="date" min={today} className={inputClass} value={checkIn}
              onChange={(e) => { setCheckIn(e.target.value); if (e.target.value && checkOut <= e.target.value) setCheckOut(addDaysISO(e.target.value, 1)) }} />
          </Field>
          <Field label="Check-out" htmlFor="bco">
            <input id="bco" type="date" min={checkIn ? addDaysISO(checkIn, 1) : today} className={inputClass} value={checkOut} onChange={(e) => setCheckOut(e.target.value)} />
          </Field>
          <Field label="Number of guests" htmlFor="bp" error={errors.people}>
            <input id="bp" type="number" min={1} max={10} className={inputClass} value={people} onChange={(e) => setPeople(e.target.value)} />
          </Field>
        </div>

        <fieldset>
          <legend className="mb-2 text-sm font-medium">Available rooms</legend>
          {dateError && <p className="rounded-lg bg-cream px-4 py-3 text-sm text-muted">Choose valid dates to see available rooms.</p>}
          {!dateError && roomsLoading && <Spinner label="Checking availability" />}
          {roomsError && <p className="text-sm text-danger" role="alert">{roomsError}</p>}
          {!dateError && !roomsLoading && rooms && rooms.length === 0 && (
            <p className="rounded-lg bg-warn-soft px-4 py-3 text-sm text-gold-deep">No room is free for these dates and guest count. Try different dates.</p>
          )}
          {!dateError && rooms && rooms.length > 0 && (
            <div className="grid max-h-60 gap-2 overflow-y-auto pr-1 sm:grid-cols-2" role="radiogroup" aria-label="Available rooms">
              {rooms.map((r) => (
                <label key={r.id} className={`flex cursor-pointer items-center justify-between gap-3 rounded-lg border p-3 transition-colors ${roomId === r.id ? 'border-forest bg-[#e9f1ec]' : 'border-line bg-paper hover:border-gold'}`}>
                  <input type="radio" name="room" className="sr-only" checked={roomId === r.id} onChange={() => setRoomId(r.id)} />
                  <span>
                    <span className="block font-medium">Room {r.roomNumber} <span className="text-xs font-normal text-muted">· {r.type}</span></span>
                    <span className="block text-xs text-muted">Up to {r.capacity} guests</span>
                  </span>
                  <span className="text-right text-sm font-semibold text-forest">{formatMoney(r.nightlyRate)}<span className="block text-[11px] font-normal text-muted">per night</span></span>
                </label>
              ))}
            </div>
          )}
          {errors.room && <p className="mt-1 text-xs font-medium text-danger" role="alert">{errors.room}</p>}
        </fieldset>

        {selectedRoom && !dateError && (
          <div className="rounded-lg border border-gold/50 bg-gold-soft/40 px-4 py-3 text-sm">
            <div className="flex items-center justify-between">
              <span>{nights} night{nights === 1 ? '' : 's'} × {formatMoney(selectedRoom.nightlyRate)}</span>
              <span className="font-display text-2xl font-semibold text-forest-dark">{formatMoney(selectedRoom.nightlyRate * nights)}</span>
            </div>
            <p className="mt-1 text-xs text-muted">Estimate before GST. The final price is calculated by the Java server when you confirm.</p>
          </div>
        )}

        <div className="flex justify-end gap-3 border-t border-line pt-4">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" loading={saving}>Confirm booking</Button>
        </div>
      </form>
    </Modal>
  )
}

const tabs = [
  { value: '', label: 'All' },
  { value: 'CONFIRMED', label: 'Confirmed' },
  { value: 'CHECKED_IN', label: 'Checked in' },
  { value: 'CHECKED_OUT', label: 'Checked out' },
  { value: 'CANCELLED', label: 'Cancelled' },
]

export default function Bookings() {
  const [params, setParams] = useSearchParams()
  const [status, setStatus] = useState('')
  const [q, setQ] = useState('')
  const [debounced, setDebounced] = useState('')
  const showNew = params.get('new') === '1'

  useEffect(() => {
    const t = window.setTimeout(() => setDebounced(q), 250)
    return () => window.clearTimeout(t)
  }, [q])

  const { data: bookings, loading, error, reload } = useAsync(() => api.bookings({ status, q: debounced }), [status, debounced])

  const closeModal = () => { setParams({}); reload() }

  return (
    <div>
      <PageHeader title="Bookings" subtitle="Reservations, arrivals and departures."
        actions={<Button icon={<Plus className="h-4 w-4" />} onClick={() => setParams({ new: '1' })}>New booking</Button>} />

      <div className="mb-5 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex flex-wrap gap-2" role="tablist" aria-label="Filter by status">
          {tabs.map((t) => (
            <button key={t.value} role="tab" aria-selected={status === t.value} onClick={() => setStatus(t.value)}
              className={`rounded-full border px-4 py-1.5 text-sm font-medium transition-colors ${status === t.value ? 'border-forest bg-forest text-ivory' : 'border-line bg-paper text-forest hover:border-gold'}`}>
              {t.label}
            </button>
          ))}
        </div>
        <div className="relative w-full lg:w-72">
          <label htmlFor="bsearch" className="sr-only">Search bookings</label>
          <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-muted" aria-hidden />
          <input id="bsearch" className={`${inputClass} pl-9`} placeholder="Guest, room or reference" value={q} onChange={(e) => setQ(e.target.value)} />
        </div>
      </div>

      {loading && !bookings && <Spinner label="Loading bookings" />}
      {error && <ErrorPanel message={error} onRetry={reload} />}
      {bookings && bookings.length === 0 && (
        <EmptyState title="No bookings here" text="Create a booking for a guest to see it listed." action={<Button icon={<Plus className="h-4 w-4" />} onClick={() => setParams({ new: '1' })}>New booking</Button>} />
      )}

      {bookings && bookings.length > 0 && (
        <div className="overflow-hidden rounded-xl border border-line bg-paper">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[760px] text-left text-sm">
              <thead className="bg-cream text-xs uppercase tracking-wider text-muted">
                <tr>
                  <th className="px-5 py-3 font-medium">Reference</th><th className="px-5 py-3 font-medium">Guest</th><th className="px-5 py-3 font-medium">Room</th>
                  <th className="px-5 py-3 font-medium">Stay</th><th className="px-5 py-3 text-right font-medium">Total</th><th className="px-5 py-3 font-medium">Status</th><th className="px-5 py-3 font-medium">Payment</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {bookings.map((b) => (
                  <tr key={b.id} className="transition-colors hover:bg-cream/60">
                    <td className="px-5 py-3.5"><Link to={`/bookings/${b.id}`} className="font-medium text-forest underline-offset-2 hover:underline">{b.reference}</Link></td>
                    <td className="px-5 py-3.5">{b.guest.name}</td>
                    <td className="px-5 py-3.5 text-muted">{b.room.roomNumber} · {b.room.type}</td>
                    <td className="px-5 py-3.5 text-muted">{formatDate(b.checkInDate)} → {formatDate(b.checkOutDate)} <span className="text-xs">({b.nights}n)</span></td>
                    <td className="px-5 py-3.5 text-right font-medium">{formatMoney(b.totalPrice)}</td>
                    <td className="px-5 py-3.5"><Badge value={b.status} /></td>
                    <td className="px-5 py-3.5"><Badge value={b.paymentStatus} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showNew && (
        <NewBookingModal
          presetRoom={params.get('room') ? Number(params.get('room')) : undefined}
          presetGuest={params.get('guest') ? Number(params.get('guest')) : undefined}
          onClose={closeModal}
        />
      )}
    </div>
  )
}
