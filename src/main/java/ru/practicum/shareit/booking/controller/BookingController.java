package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInnerDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import java.util.List;

import static ru.practicum.shareit.consts.ShareItAppConst.HEADER_CALLER_ID;
import static ru.practicum.shareit.log.Logger.logRequest;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingOutDto addBooking(@RequestHeader(HEADER_CALLER_ID) long bookerId,
                                    @Valid @RequestBody BookingInnerDto bookingInnerDto) {
        logRequest(HttpMethod.POST, "/bookings",
                HEADER_CALLER_ID + bookerId, bookingInnerDto.toString());
        return bookingService.addBooking(bookerId, bookingInnerDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutDto updateBooking(@RequestHeader(HEADER_CALLER_ID) long ownerId,
                                       @PathVariable long bookingId,
                                       @RequestParam boolean approved) {
        logRequest(HttpMethod.PATCH, String.format("/bookings/%s?approved=%s", bookingId, approved),
                HEADER_CALLER_ID + ownerId, "no");
        return bookingService.updateBooking(ownerId, bookingId, approved);

    }

    @GetMapping("/{bookingId}")
    public BookingOutDto getBookingById(@RequestHeader(HEADER_CALLER_ID) long userId,
                                        @PathVariable long bookingId) {
        logRequest(HttpMethod.GET, "/bookings/" + bookingId,
                HEADER_CALLER_ID + userId, "no");
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutDto> getBookingsForBooker(@RequestHeader(HEADER_CALLER_ID) long bookerId,
                                                    @RequestParam(defaultValue = "ALL") String state,
                                                    @RequestParam(required = false) @PositiveOrZero Integer from,
                                                    @RequestParam(required = false) @Positive Integer size) {
        logRequest(HttpMethod.GET, String.format("/bookings?state=%s&from=%s&size=%s", state, from, size),
                HEADER_CALLER_ID + bookerId, "no");
        return bookingService.getBookingsForBooker(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> getBookingsForOwner(@RequestHeader(HEADER_CALLER_ID) long ownerId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(required = false) @PositiveOrZero Integer from,
                                                   @RequestParam(required = false) @Positive Integer size) {
        logRequest(HttpMethod.GET, String.format("/bookings/owner?state=%s&from=%s&size=%s", state, from, size),
                HEADER_CALLER_ID + ownerId, "no");
        return bookingService.getBookingsForOwner(ownerId, state, from, size);
    }
}
