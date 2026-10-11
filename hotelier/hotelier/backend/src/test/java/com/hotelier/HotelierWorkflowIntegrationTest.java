package com.hotelier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end test of the main workflow through the real controllers, services, JPA and an in-memory H2 database.
 * Run with:  mvn test
 */
@SpringBootTest
@AutoConfigureMockMvc
class HotelierWorkflowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private static long idOf(String json) {
        Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(json);
        if (!m.find()) {
            throw new AssertionError("no id in " + json);
        }
        return Long.parseLong(m.group(1));
    }

    private long createGuest(String name, String phone) throws Exception {
        String body = mvc.perform(post("/api/guests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"phone\":\"" + phone + "\",\"email\":\"t@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestCode").exists())
                .andReturn().getResponse().getContentAsString();
        return idOf(body);
    }

    private long roomId(String number) throws Exception {
        String body = mvc.perform(get("/api/rooms").param("q", number)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return idOf(body);
    }

    private String bookingJson(long guestId, long roomId, LocalDate in, LocalDate out, int guests) {
        return "{\"guestId\":" + guestId + ",\"roomId\":" + roomId + ",\"checkInDate\":\"" + in
                + "\",\"checkOutDate\":\"" + out + "\",\"numberOfGuests\":" + guests + "}";
    }

    @Test
    void fullWorkflow() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate in = today.plusDays(20);
        LocalDate out = today.plusDays(22);

        // 1. rooms are seeded
        mvc.perform(get("/api/rooms")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(10));

        // 2-4. guest + booking + Java-calculated price (suite 302: 8200 * 1.40 = 11480 per night, 2 nights)
        long guest = createGuest("Test Guest", "9111111111");
        long suite = roomId("302");
        String created = mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(guest, suite, in, out, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(22960.0))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn().getResponse().getContentAsString();
        long bookingId = idOf(created.substring(created.indexOf("\"id\":")));

        // 5. overlapping reservation and invalid dates are rejected
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(guest, suite, in.plusDays(1), out.plusDays(1), 2)))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(guest, suite, out, in, 2)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(guest, suite, in, out.plusDays(1), 9)))
                .andExpect(status().isBadRequest());   // 9 guests exceed the suite capacity

        // too early to check in
        mvc.perform(post("/api/bookings/" + bookingId + "/check-in")).andExpect(status().isConflict());

        // 6. invoice exists, cancel works once
        mvc.perform(get("/api/bookings/" + bookingId + "/invoice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(25715.2));
        mvc.perform(post("/api/bookings/" + bookingId + "/cancel"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(post("/api/bookings/" + bookingId + "/cancel")).andExpect(status().isConflict());

        // after cancelling, the same dates are free again
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(guest, suite, in, out, 2)))
                .andExpect(status().isCreated());
    }

    @Test
    void checkInAndCheckOutToday_updateRoomAvailability() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        long guest = createGuest("Stay Guest", "9222222222");
        long room = roomId("104");
        String created = mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(guest, room, today, today.plusDays(1), 2)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long bookingId = idOf(created.substring(created.indexOf("\"id\":")));

        mvc.perform(post("/api/bookings/" + bookingId + "/check-in"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.room.status").value("OCCUPIED"));
        mvc.perform(post("/api/bookings/" + bookingId + "/cancel")).andExpect(status().isConflict());
        mvc.perform(post("/api/bookings/" + bookingId + "/check-out"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CHECKED_OUT"))
                .andExpect(jsonPath("$.room.status").value("AVAILABLE"));
        mvc.perform(post("/api/bookings/" + bookingId + "/pay"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.paymentStatus").value("PAID"));
    }

    @Test
    void dashboardAndHealthUseLiveData() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/api/dashboard")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms").value(10))
                .andExpect(jsonPath("$.staffOnDesk.length()").value(2));
    }
}
