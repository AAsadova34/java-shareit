package ru.practicum.shareit.booking.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInnerDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {
    @Transactional
    BookingOutDto addBooking(long bookerId, BookingInnerDto bookingInnerDto);

    @Transactional
    BookingOutDto updateBooking(long ownerId, long bookingId, boolean approved);

    @Transactional(readOnly = true)
    BookingOutDto getBookingById(long userId, long bookingId);

    @Transactional(readOnly = true)
    List<BookingOutDto> getBookingsForBooker(long bookerId, String state);

    @Transactional(readOnly = true)
    List<BookingOutDto> getBookingsForOwner(long ownerId, String state);
}
