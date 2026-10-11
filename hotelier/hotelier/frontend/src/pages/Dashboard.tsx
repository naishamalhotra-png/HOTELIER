import { Link } from 'react-router-dom'
import { ArrowRight, BedDouble, CalendarCheck, CalendarClock, DoorOpen, Gauge, IndianRupee, KeyRound, Plus, UserRound, Users } from 'lucide-react'
import { api } from '../api'
import { useAsync } from '../useAsync'
import { Badge, Button, Card, EmptyState, ErrorPanel, Spinner } from '../components/ui'
import { formatDate, formatMoney } from '../format'
import type { ReactNode } from 'react'

function greeting() {
  const h = new Date().getHours()
  return h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening'
}

function Stat({ icon, label, value, delay, accent }: { icon: ReactNode; label: string; value: string | number; delay: number; accent?: boolean }) {
  return (
    <div className={`fade-up rounded-xl border p-5 ${accent ? 'border-gold/60 bg-gold-soft/50' : 'border-line bg-paper'}`} style={{ animationDelay: `${delay}ms` }}>
      <div className="flex items-center justify-between text-forest">{icon}</div>
      <div className="mt-4 font-display text-4xl font-semibold text-forest-dark">{value}</div>
      <div className="mt-0.5 text-sm text-muted">{label}</div>
    </div>
  )
}

export default function Dashboard() {
  const { data, loading, error, reload } = useAsync(() => api.dashboard(), [])
  const today = new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })

  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-forest-dark via-forest to-teal-deep px-7 py-9 text-ivory sm:px-10 sm:py-11">
        <svg className="pointer-events-none absolute -right-10 -top-10 h-72 w-72 text-gold/20" viewBox="0 0 200 200" fill="none" aria-hidden>
          <circle cx="100" cy="100" r="90" stroke="currentColor" strokeWidth="1" />
          <circle cx="100" cy="100" r="68" stroke="currentColor" strokeWidth="1" />
          <circle cx="100" cy="100" r="46" stroke="currentColor" strokeWidth="1" />
          <path d="M100 10v180M10 100h180" stroke="currentColor" strokeWidth="1" />
        </svg>
        <p className="text-xs uppercase tracking-[0.3em] text-gold">{today}</p>
        <h1 className="mt-3 text-4xl font-semibold sm:text-5xl">{greeting()}, welcome to HOTELIER</h1>
        <p className="mt-2 max-w-xl text-sm text-ivory/80">Here is how the hotel looks right now, straight from the live database.</p>
        <div className="mt-6 flex flex-wrap gap-3">
          <Link to="/bookings?new=1"><Button className="bg-gold !text-forest-dark hover:bg-gold-soft" icon={<Plus className="h-4 w-4" />}>New booking</Button></Link>
          <Link to="/guests"><Button variant="secondary" className="!border-ivory/30 !bg-transparent !text-ivory hover:!bg-ivory/10" icon={<Users className="h-4 w-4" />}>Manage guests</Button></Link>
        </div>
      </section>

      {loading && <Spinner label="Loading the dashboard" />}
      {error && <div className="mt-8"><ErrorPanel message={error} onRetry={reload} /></div>}

      {data && (
        <>
          <section className="mt-8 grid grid-cols-2 gap-4 lg:grid-cols-4" aria-label="Room summary">
            <Stat delay={0} icon={<BedDouble className="h-5 w-5" />} label="Total rooms" value={data.totalRooms} />
            <Stat delay={60} icon={<DoorOpen className="h-5 w-5" />} label="Available rooms" value={data.availableRooms} />
            <Stat delay={120} icon={<KeyRound className="h-5 w-5" />} label="Occupied rooms" value={data.occupiedRooms} />
            <Stat delay={180} accent icon={<Gauge className="h-5 w-5" />} label="Occupancy" value={`${data.occupancyPercent}%`} />
          </section>

          <Card className="fade-up mt-4 p-5" >
            <div className="flex items-center justify-between text-sm">
              <h2 className="text-2xl font-semibold text-forest-dark">Occupancy overview</h2>
              <span className="text-muted">{data.occupiedRooms} of {data.totalRooms} rooms in use</span>
            </div>
            <div className="mt-4 flex h-3 overflow-hidden rounded-full bg-cream" role="img" aria-label={`${data.occupiedRooms} occupied, ${data.availableRooms} available, ${data.maintenanceRooms} in maintenance`}>
              <div className="bg-forest transition-all duration-700" style={{ width: `${data.totalRooms ? (data.occupiedRooms / data.totalRooms) * 100 : 0}%` }} />
              <div className="bg-gold/70 transition-all duration-700" style={{ width: `${data.totalRooms ? (data.maintenanceRooms / data.totalRooms) * 100 : 0}%` }} />
            </div>
            <div className="mt-3 flex flex-wrap gap-x-6 gap-y-1 text-xs text-muted">
              <span className="flex items-center gap-2"><i className="h-2.5 w-2.5 rounded-full bg-forest" /> Occupied</span>
              <span className="flex items-center gap-2"><i className="h-2.5 w-2.5 rounded-full bg-gold/70" /> Maintenance ({data.maintenanceRooms})</span>
              <span className="flex items-center gap-2"><i className="h-2.5 w-2.5 rounded-full bg-cream ring-1 ring-line" /> Available</span>
            </div>
          </Card>

          <section className="mt-4 grid grid-cols-2 gap-4 lg:grid-cols-4" aria-label="Activity summary">
            <Stat delay={0} icon={<Users className="h-5 w-5" />} label="Registered guests" value={data.totalGuests} />
            <Stat delay={60} icon={<CalendarCheck className="h-5 w-5" />} label="Active bookings" value={data.activeBookings} />
            <Stat delay={120} icon={<CalendarClock className="h-5 w-5" />} label="Upcoming arrivals" value={data.upcomingArrivals} />
            <Stat delay={180} icon={<IndianRupee className="h-5 w-5" />} label="Paid revenue" value={formatMoney(data.paidRevenue)} />
          </section>

          <div className="mt-8 grid gap-6 lg:grid-cols-3">
            <section className="lg:col-span-2" aria-label="Current bookings">
              <div className="mb-3 flex items-center justify-between">
                <h2 className="text-3xl font-semibold text-forest-dark">Current bookings</h2>
                <Link to="/bookings" className="flex items-center gap-1 text-sm font-medium text-forest hover:underline">View all <ArrowRight className="h-4 w-4" /></Link>
              </div>
              {data.currentBookings.length === 0 ? (
                <EmptyState title="No active bookings" text="Create a booking to see it here." action={<Link to="/bookings?new=1"><Button icon={<Plus className="h-4 w-4" />}>New booking</Button></Link>} />
              ) : (
                <div className="space-y-3">
                  {data.currentBookings.map((b, i) => (
                    <Link key={b.id} to={`/bookings/${b.id}`} className="card-lift fade-up flex items-center justify-between gap-4 rounded-xl border border-line bg-paper p-4" style={{ animationDelay: `${i * 50}ms` }}>
                      <div className="min-w-0">
                        <div className="truncate font-medium text-ink">{b.guest.name}</div>
                        <div className="mt-0.5 text-xs text-muted">{b.reference} · Room {b.room.roomNumber} · {formatDate(b.checkInDate)} to {formatDate(b.checkOutDate)}</div>
                      </div>
                      <Badge value={b.status} />
                    </Link>
                  ))}
                </div>
              )}
            </section>

            <section aria-label="Front desk">
              <h2 className="mb-3 text-3xl font-semibold text-forest-dark">Front desk</h2>
              <Card className="divide-y divide-line">
                {data.staffOnDesk.length === 0 && <p className="p-5 text-sm text-muted">No staff records.</p>}
                {data.staffOnDesk.map((s) => (
                  <div key={s.id} className="flex gap-3 p-4">
                    <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-gold-soft text-gold-deep"><UserRound className="h-5 w-5" aria-hidden /></span>
                    <div>
                      <div className="text-sm font-medium">{s.name}</div>
                      <div className="text-xs text-muted">{s.role} · {s.shift} shift</div>
                      <div className="mt-1 text-xs text-muted">{s.duty}</div>
                    </div>
                  </div>
                ))}
              </Card>
            </section>
          </div>
        </>
      )}
    </div>
  )
}
