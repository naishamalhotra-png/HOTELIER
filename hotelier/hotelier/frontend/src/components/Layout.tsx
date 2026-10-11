import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { BedDouble, CalendarCheck, LayoutDashboard, Menu, Users, X } from 'lucide-react'

const nav = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/rooms', label: 'Rooms', icon: BedDouble, end: false },
  { to: '/guests', label: 'Guests', icon: Users, end: false },
  { to: '/bookings', label: 'Bookings', icon: CalendarCheck, end: false },
]

function Brand() {
  return (
    <div>
      <div className="font-display text-3xl font-semibold tracking-[0.18em] text-ivory">HOTELIER</div>
      <div className="mt-1 flex items-center gap-2 text-[10px] uppercase tracking-[0.3em] text-gold">
        <span className="h-px w-6 bg-gold" aria-hidden /> Boutique Hotel
      </div>
    </div>
  )
}

function NavLinks({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <nav aria-label="Main" className="mt-10 flex flex-col gap-1">
      {nav.map(({ to, label, icon: Icon, end }) => (
        <NavLink
          key={to}
          to={to}
          end={end}
          onClick={onNavigate}
          className={({ isActive }) =>
            `flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium transition-colors ${
              isActive ? 'bg-ivory/12 text-ivory shadow-[inset_3px_0_0_var(--color-gold)]' : 'text-ivory/75 hover:bg-ivory/8 hover:text-ivory'
            }`
          }
        >
          <Icon className="h-[18px] w-[18px]" aria-hidden />
          {label}
        </NavLink>
      ))}
    </nav>
  )
}

export default function Layout() {
  const [open, setOpen] = useState(false)
  const location = useLocation()
  useEffect(() => { setOpen(false) }, [location.pathname])

  return (
    <div className="min-h-screen lg:flex">
      {/* Desktop sidebar */}
      <aside className="no-print hidden w-64 shrink-0 bg-forest-dark px-6 py-9 lg:sticky lg:top-0 lg:block lg:h-screen">
        <Brand />
        <NavLinks />
        <p className="absolute bottom-6 left-6 right-6 text-xs leading-relaxed text-ivory/50">
          Java OOPS mini-project. Sample data is fictional.
        </p>
      </aside>

      {/* Mobile top bar */}
      <header className="no-print sticky top-0 z-40 flex items-center justify-between bg-forest-dark px-5 py-3.5 lg:hidden">
        <div className="font-display text-2xl font-semibold tracking-[0.16em] text-ivory">HOTELIER</div>
        <button onClick={() => setOpen(true)} aria-label="Open menu" className="rounded-lg p-2 text-ivory hover:bg-ivory/10">
          <Menu className="h-6 w-6" />
        </button>
      </header>

      {open && (
        <div className="no-print fixed inset-0 z-50 lg:hidden">
          <div className="backdrop-enter absolute inset-0 bg-forest-dark/60" onClick={() => setOpen(false)} aria-hidden />
          <div className="drawer-enter relative h-full w-72 bg-forest-dark px-6 py-9">
            <button onClick={() => setOpen(false)} aria-label="Close menu" className="absolute right-4 top-4 rounded-lg p-2 text-ivory hover:bg-ivory/10">
              <X className="h-5 w-5" />
            </button>
            <Brand />
            <NavLinks onNavigate={() => setOpen(false)} />
          </div>
        </div>
      )}

      <main className="min-w-0 flex-1 px-5 py-8 sm:px-8 lg:px-12 lg:py-10">
        <div key={location.pathname} className="page-enter mx-auto max-w-6xl">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
