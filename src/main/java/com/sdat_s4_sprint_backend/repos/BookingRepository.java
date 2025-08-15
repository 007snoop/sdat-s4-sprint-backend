package com.sdat_s4_sprint_backend.repos;

import com.sdat_s4_sprint_backend.entity.Booking;
import com.sdat_s4_sprint_backend.entity.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByPassenger_Id(Long passengerId);
    List<Booking> findByFlight_Id(Long flightId);
    Optional<Booking> findByBookingRef(String bookingRef);

    @Query("""
         select b from Booking b
         where b.passenger.id = :pid
           and b.status in (com.sdat_s4_sprint_backend.entity.Booking.BookingStatus.CONFIRMED,
                            com.sdat_s4_sprint_backend.entity.Booking.BookingStatus.CHECKED_IN,
                            com.sdat_s4_sprint_backend.entity.Booking.BookingStatus.BOARDED)
         """)
    List<Booking> findActiveByPassengerId(Long pid);

    long countByFlight_IdAndStatus(Long flightId, BookingStatus status);
}
