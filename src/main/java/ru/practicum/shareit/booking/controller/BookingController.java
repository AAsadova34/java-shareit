package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInnerDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;

import java.util.List;

import static ru.practicum.shareit.log.Logger.logRequest;

/**
 * TODO Sprint add-bookings.
 */

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingOutDto addBooking(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                    @Valid @RequestBody BookingInnerDto bookingInnerDto) {
        logRequest(HttpMethod.POST, "/bookings",
                "X-Sharer-User-Id-" + bookerId, bookingInnerDto.toString());
        return bookingService.addBooking(bookerId, bookingInnerDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutDto updateBooking(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                       @PathVariable long bookingId,
                                       @RequestParam boolean approved) {
        logRequest(HttpMethod.PATCH, String.format("/bookings/%s?approved=%s", bookingId, approved),
                "X-Sharer-User-Id-" + ownerId, "no");
        return bookingService.updateBooking(ownerId, bookingId, approved);

    }

    @GetMapping("/{bookingId}")
    public BookingOutDto getBookingById(@RequestHeader(name = "X-Sharer-User-Id") long userId,
                                        @PathVariable long bookingId) {
        logRequest(HttpMethod.GET, "/bookings/" + bookingId,
                "X-Sharer-User-Id-" + userId, "no");
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutDto> getBookingsForBooker(@RequestHeader(name = "X-Sharer-User-Id") long bookerId,
                                                    @RequestParam(defaultValue = "ALL") String state) {
        logRequest(HttpMethod.GET, "/bookings?state=" + state,
                "X-Sharer-User-Id-" + bookerId, "no");
        return bookingService.getBookingsForBooker(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> getBookingsForOwner(@RequestHeader(name = "X-Sharer-User-Id") long ownerId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        logRequest(HttpMethod.GET, "/bookings/owner?state=" + state,
                "X-Sharer-User-Id-" + ownerId, "no");
        return bookingService.getBookingsForOwner(ownerId, state);
    }
}
