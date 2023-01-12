package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Booking save(Booking booking);

    Booking getReferenceById(long bookingId);

    List<Booking> findAllByBookerIdOrderByStartDesc(long bookerId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime dataTime);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime dataTime);

    @Query("SELECT new Booking(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "FROM Booking AS b " +
            "WHERE b.booker.id = ?1 AND ?2 BETWEEN b.start AND b.end " +
            "ORDER BY b.start desc")
    List<Booking> findAllByBookerIdAndCurrent(long bookerId, LocalDateTime dataTime);

    @Query("SELECT new Booking(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "FROM Booking AS b " +
            "WHERE b.item.userId = ?1 " +
            "ORDER BY b.start desc")
    List<Booking> findAllByOwnerId(long ownerId);

    @Query("SELECT new Booking(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "FROM Booking AS b " +
            "WHERE b.item.userId = ?1 AND b.status = ?2 " +
            "ORDER BY b.start desc")
    List<Booking> findAllByOwnerIdAndStatus(long ownerId, BookingStatus status);

    @Query("SELECT new Booking(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "FROM Booking AS b " +
            "WHERE b.item.userId = ?1 AND b.end < ?2  " +
            "ORDER BY b.start desc")
    List<Booking> findAllByOwnerIdAndPast(long ownerId, LocalDateTime dataTime);

    @Query("SELECT new Booking(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "FROM Booking AS b " +
            "WHERE b.item.userId = ?1 AND ?2 < b.start " +
            "ORDER BY b.start desc")
    List<Booking> findAllByOwnerIdAndFuture(long ownerId, LocalDateTime dataTime);

    @Query("SELECT new Booking(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "FROM Booking AS b " +
            "WHERE b.item.userId = ?1 AND ?2 BETWEEN b.start AND b.end " +
            "ORDER BY b.start desc")
    List<Booking> findAllByOwnerIdAndCurrent(long ownerId, LocalDateTime dataTime);

    @Query(value = "SELECT b.* " +
            "FROM bookings AS b " +
            "WHERE b.item_id = ?1 AND ?2 > b.start_time " +
            "ORDER BY b.start_time desc " +
            "LIMIT 1", nativeQuery = true)
    Optional<Booking> findLastByItemId(long itemId, LocalDateTime dataTime);

    @Query(value = "SELECT b.* " +
            "FROM bookings AS b " +
            "WHERE b.item_id = ?1 AND ?2 < b.start_time " +
            "ORDER BY b.start_time " +
            "LIMIT 1", nativeQuery = true)
    Optional<Booking> findNextByItemId(long itemId, LocalDateTime dataTime);

    @Query("SELECT new Booking(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "FROM Booking AS b " +
            "WHERE b.item.id = ?1 AND b.booker.id = ?2 AND ?3 > b.end")
    List<Booking> findAllByBookerAndFinished(long itemId, long bookerId, LocalDateTime dataTime);
}
