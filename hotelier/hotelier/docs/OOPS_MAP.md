# OOPS concept → source-code map

All paths are under `backend/src/main/java/com/hotelier/`. Open the file, search for the method or keyword in the last column.

| # | Concept | File | What to point at |
|---|---|---|---|
| 1 | Class & object | `model/Booking.java`, `model/Room.java`, `model/Guest.java` | Real hotel objects. `new Booking(guest, room, ...)` in `BookingService.createBooking` |
| 2 | Encapsulation | `model/Person.java`, `model/Room.java`, `model/Booking.java` | All fields `private`. Room status changes only via `markOccupied()`, `markAvailable()`, `markMaintenance()`. Booking has **no setters**; status changes only via `checkIn()`, `checkOut()`, `cancel()`, `markPaid()` |
| 3 | Constructors | `model/Person.java`, `model/Room.java` | Parameterised constructors validate data (blank name / zero price → exception). A `protected` no-arg constructor exists only for JPA |
| 4 | `this` | `model/Person.java`, `model/Booking.java` | `this(name, phone, null)` (constructor chaining); `this.name = ...` |
| 5 | `super` | `model/Guest.java`, `model/Employee.java`, `model/StandardRoom.java` | `super(name, phone)` calls the parent constructor; `super.describe()` calls the parent method |
| 6 | Constructor overloading | `Person` (2), `Guest` (2), `Booking` (2) | Same constructor name, different parameters |
| 7 | Method overloading | `model/Room.java` | `calculateCost(int nights)` and `calculateCost(LocalDate, LocalDate)` |
| 8 | Inheritance (multilevel) | `Person → Employee → Receptionist` | `model/Person.java`, `Employee.java`, `Receptionist.java`. Also `Person → Guest` and `Room → StandardRoom/DeluxeRoom/Suite` |
| 9 | Abstract class | `model/Person.java`, `model/Employee.java`, `model/Room.java` | `public abstract class` – cannot be instantiated |
| 10 | Abstract method | `Person.getRole()`, `Employee.getDuty()`, `Room.nightlyRate()`, `Room.getRoomType()` | Declared without body; children must implement |
| 11 | Method overriding | `Guest.getRole()`, `Guest.describe()`, `Receptionist.getRole()`, `DeluxeRoom.nightlyRate()`, `Suite.nightlyRate()` | `@Override` |
| 12 | Runtime polymorphism | `service/RoomService.create()`; `dto/RoomResponse.from(Room)`; `service/HotelService.getDashboard()`; `model/Booking` constructor | `Room room = new Suite(...)`; `room.nightlyRate()` runs the **child's** version. `List<Employee>` holding `Receptionist` objects. Proven in `OopsConceptsTest` |
| 13 | Interface | `model/Billable.java` → implemented by `model/Invoice.java` | Contract: `getLineItems()`, `calculateSubtotal()`, `calculateTotal()`; includes a `default` method `calculateTax()` and constant `GST_RATE` |
| 14 | Composition / has-a | `model/Booking.java`, `model/Invoice.java` | Booking HAS-A Guest, HAS-A Room. Invoice HAS-A Booking. Contrast with *is-a*: Guest IS-A Person |
| 15 | Access modifiers | everywhere | `private` fields; `public` methods; `protected` constructors in abstract parents and JPA no-arg constructors; `private static` helpers (`Person.requireText`) |
| 16 | Custom exceptions | `exception/` | `HotelException` → `BookingException` → `RoomUnavailableException`, `InvalidDatesException`, `InvalidStateException` (exception inheritance) |
| 17 | `throw` | `Booking.checkIn()`, `BookingValidator.validateDates()`, `BookingService.createBooking()` | Real validation, not demo code |
| 18 | `throws` | `Booking.checkIn() throws ...`, `InvoiceExporter.export() throws IOException`, `GuestService.create() throws ValidationException` | Method declares what it may throw |
| 19 | `try / catch` | `service/RoomService.applyStatusChange()`, `config/DataSeeder.run()`, `service/BillingService.exportInvoice()` | Converts or handles errors |
| 20 | `finally` | `service/InvoiceExporter.export()` | File writer is always closed |
| 21 | File handling | `service/InvoiceExporter.java` | Saves invoice `.txt` on the server and returns it for download |
| 22 | Layered architecture | `controller/` → `service/` → `repository/` → database | Business rules live in `service/` and `model/`, not in controllers |
| 23 | Interface (Spring Data) | `repository/*Repository.java` | Interfaces extending `JpaRepository`; Spring generates the implementation |

## Inheritance trees

```
Person (abstract)                     Room (abstract)
 ├── Guest                             ├── StandardRoom   (rate = base)
 └── Employee (abstract)               ├── DeluxeRoom     (rate = base × 1.15)
      └── Receptionist                 └── Suite          (rate = base × 1.40)

HotelException (RuntimeException)      Billable (interface)
 ├── BookingException                   └── Invoice
 │    ├── RoomUnavailableException
 │    ├── InvalidDatesException
 │    └── InvalidStateException
 ├── ResourceNotFoundException
 └── ValidationException
```
