export type RoomStatus = 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'
export type BookingStatus = 'CONFIRMED' | 'CHECKED_IN' | 'CHECKED_OUT' | 'CANCELLED'
export type PaymentStatus = 'PENDING' | 'PAID'
export type RoomTypeInput = 'STANDARD' | 'DELUXE' | 'SUITE'

export interface Guest {
  id: number
  guestCode: string
  name: string
  phone: string
  email: string | null
}

export interface Room {
  id: number
  roomNumber: string
  type: string // "Standard" | "Deluxe" | "Suite" (comes from the Java getRoomType())
  capacity: number
  baseRate: number
  nightlyRate: number
  status: RoomStatus
}

export interface Booking {
  id: number
  reference: string
  guest: Guest
  room: Room
  checkInDate: string
  checkOutDate: string
  nights: number
  numberOfGuests: number
  nightlyRate: number
  totalPrice: number
  status: BookingStatus
  paymentStatus: PaymentStatus
  createdAt: string | null
  checkedInAt: string | null
  checkedOutAt: string | null
}

export interface Staff {
  id: number
  name: string
  role: string
  shift: string
  duty: string
}

export interface Dashboard {
  totalRooms: number
  availableRooms: number
  occupiedRooms: number
  maintenanceRooms: number
  occupancyPercent: number
  totalGuests: number
  activeBookings: number
  checkedInNow: number
  upcomingArrivals: number
  paidRevenue: number
  currentBookings: Booking[]
  staffOnDesk: Staff[]
}

export interface InvoiceLine {
  description: string
  quantity: number
  unitPrice: number
  amount: number
}

export interface Invoice {
  invoiceNumber: string
  issuedOn: string
  bookingReference: string
  guestName: string
  guestCode: string
  guestPhone: string
  roomNumber: string
  roomType: string
  checkInDate: string
  checkOutDate: string
  nights: number
  lines: InvoiceLine[]
  subtotal: number
  gstPercent: number
  tax: number
  total: number
  bookingStatus: BookingStatus
  paymentStatus: PaymentStatus
}
