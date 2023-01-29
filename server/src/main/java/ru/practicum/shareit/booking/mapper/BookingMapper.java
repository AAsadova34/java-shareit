package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.dto.BookingInnerDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {
    public static Booking toBooking(BookingInnerDto bookingInnerDto,
                                    Item item,
                                    User booker,
                                    BookingStatus status) {
        Booking booking = new Booking();
        booking.setStart(bookingInnerDto.getStart());
        booking.setEnd(bookingInnerDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }

    public static BookingOutDto toBookingOutDto(Booking booking,
                                                ItemOutShortDto itemOutShortDto,
                                                UserDto bookerDto) {
        BookingOutDto bookingOutDto = new BookingOutDto();
        bookingOutDto.setItem(itemOutShortDto);
        bookingOutDto.setBooker(bookerDto);
        bookingOutDto.setId(booking.getId());
        bookingOutDto.setStart(booking.getStart());
        bookingOutDto.setEnd(booking.getEnd());
        bookingOutDto.setStatus(booking.getStatus());
        return bookingOutDto;
    }

    public static BookingForItemDto toBookingForItemDto(Booking booking) {
        BookingForItemDto bookingForItemDto = new BookingForItemDto();
        bookingForItemDto.setId(booking.getId());
        bookingForItemDto.setBookerId(booking.getBooker().getId());
        bookingForItemDto.setItemId(booking.getItem().getId());
        bookingForItemDto.setStart(booking.getStart());
        bookingForItemDto.setEnd(booking.getEnd());
        return bookingForItemDto;
    }

}
