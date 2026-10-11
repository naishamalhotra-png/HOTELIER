import type { Booking, Dashboard, Guest, Invoice, Room } from './types'

const BASE = ((import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:8080').replace(/\/+$/, '')

export class ApiError extends Error {
  status: number
  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  let response: Response
  try {
    response = await fetch(BASE + path, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...(options.headers ?? {}) },
    })
  } catch {
    throw new ApiError(
      'Cannot reach the server. If it is on free hosting it may be waking up - wait about a minute and try again.',
      0,
    )
  }
  if (!response.ok) {
    let message = `Request failed (${response.status})`
    try {
      const body = await response.json()
      if (body && typeof body.message === 'string') message = body.message
    } catch {
      /* keep the default message */
    }
    throw new ApiError(message, response.status)
  }
  const text = await response.text()
  return (text ? JSON.parse(text) : undefined) as T
}

const json = (body: unknown) => ({ body: JSON.stringify(body) })
const qs = (params: Record<string, string | number | undefined | null>) => {
  const p = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') p.set(k, String(v))
  })
  const s = p.toString()
  return s ? `?${s}` : ''
}

export interface GuestInput { name: string; phone: string; email: string }
export interface RoomInput { roomNumber: string; type: string; capacity: number; baseRate: number; status?: string }
export interface BookingInput { guestId: number; roomId: number; checkInDate: string; checkOutDate: string; numberOfGuests: number }

export const api = {
  dashboard: () => request<Dashboard>('/api/dashboard'),

  guests: (q?: string) => request<Guest[]>('/api/guests' + qs({ q })),
  createGuest: (g: GuestInput) => request<Guest>('/api/guests', { method: 'POST', ...json(g) }),
  updateGuest: (id: number, g: GuestInput) => request<Guest>(`/api/guests/${id}`, { method: 'PUT', ...json(g) }),

  rooms: (f: { q?: string; type?: string; status?: string; minCapacity?: number | string } = {}) =>
    request<Room[]>('/api/rooms' + qs(f)),
  availableRooms: (checkIn: string, checkOut: string, guests: number) =>
    request<Room[]>('/api/rooms/available' + qs({ checkIn, checkOut, guests })),
  createRoom: (r: RoomInput) => request<Room>('/api/rooms', { method: 'POST', ...json(r) }),
  updateRoom: (id: number, r: RoomInput) => request<Room>(`/api/rooms/${id}`, { method: 'PUT', ...json(r) }),

  bookings: (f: { status?: string; q?: string } = {}) => request<Booking[]>('/api/bookings' + qs(f)),
  booking: (id: number | string) => request<Booking>(`/api/bookings/${id}`),
  createBooking: (b: BookingInput) => request<Booking>('/api/bookings', { method: 'POST', ...json(b) }),
  cancelBooking: (id: number) => request<Booking>(`/api/bookings/${id}/cancel`, { method: 'POST' }),
  checkIn: (id: number) => request<Booking>(`/api/bookings/${id}/check-in`, { method: 'POST' }),
  checkOut: (id: number) => request<Booking>(`/api/bookings/${id}/check-out`, { method: 'POST' }),
  pay: (id: number) => request<Booking>(`/api/bookings/${id}/pay`, { method: 'POST' }),
  invoice: (id: number | string) => request<Invoice>(`/api/bookings/${id}/invoice`),

  /** Downloads the invoice as a .txt file (the Java backend also saves a copy on the server). */
  async downloadInvoice(id: number | string): Promise<void> {
    let response: Response
    try {
      response = await fetch(`${BASE}/api/bookings/${id}/invoice/export`)
    } catch {
      throw new ApiError('Cannot reach the server.', 0)
    }
    if (!response.ok) throw new ApiError('Could not export the invoice.', response.status)
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `invoice-${id}.txt`
    a.click()
    URL.revokeObjectURL(url)
  },
}
