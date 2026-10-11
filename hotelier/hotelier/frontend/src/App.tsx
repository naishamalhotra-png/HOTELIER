import { HashRouter, Route, Routes, Link } from 'react-router-dom'
import Layout from './components/Layout'
import { ToastProvider } from './components/Toast'
import Dashboard from './pages/Dashboard'
import Rooms from './pages/Rooms'
import Guests from './pages/Guests'
import Bookings from './pages/Bookings'
import BookingDetail from './pages/BookingDetail'
import InvoicePage from './pages/InvoicePage'

function NotFound() {
  return (
    <div className="py-24 text-center">
      <h1 className="text-5xl font-semibold text-forest-dark">Page not found</h1>
      <p className="mt-2 text-muted">That page does not exist.</p>
      <Link to="/" className="mt-6 inline-block text-forest underline">Back to the dashboard</Link>
    </div>
  )
}

/** HashRouter keeps the site working on any static host without extra rewrite rules. */
export default function App() {
  return (
    <ToastProvider>
      <HashRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/rooms" element={<Rooms />} />
            <Route path="/guests" element={<Guests />} />
            <Route path="/bookings" element={<Bookings />} />
            <Route path="/bookings/:id" element={<BookingDetail />} />
            <Route path="/bookings/:id/invoice" element={<InvoicePage />} />
            <Route path="*" element={<NotFound />} />
          </Route>
        </Routes>
      </HashRouter>
    </ToastProvider>
  )
}
