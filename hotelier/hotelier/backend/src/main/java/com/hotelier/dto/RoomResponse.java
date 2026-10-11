package com.hotelier.dto;

import com.hotelier.model.Room;
import java.math.BigDecimal;

public record RoomResponse(Long id, String roomNumber, String type, int capacity,
                           BigDecimal baseRate, BigDecimal nightlyRate, String status) {

    /** The parameter is the PARENT type Room; the real object may be a Suite, DeluxeRoom or StandardRoom. */
    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getRoomNumber(), room.getRoomType(), room.getCapacity(),
                room.getBaseRate(), room.nightlyRate(), room.getStatus().name());
    }
}
