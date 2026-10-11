package com.hotelier.repository;

import com.hotelier.model.Booking;
import com.hotelier.model.BookingStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b order by b.createdAt desc, b.id desc")
    List<Booking> findAllNewestFirst();

    @Query("select b from Booking b where b.room.id = :roomId and b.status in :statuses")
    List<Booking> findByRoomAndStatuses(@Param("roomId") Long roomId,
                                        @Param("statuses") Collection<BookingStatus> statuses);

    List<Booking> findByStatusIn(Collection<BookingStatus> statuses);

    long countByStatus(BookingStatus status);
}
