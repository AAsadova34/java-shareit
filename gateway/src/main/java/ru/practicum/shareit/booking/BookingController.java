package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInnerDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ValidationException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.consts.ShareItAppConst.HEADER_CALLER_ID;
import static ru.practicum.shareit.log.Logger.logRequest;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(HEADER_CALLER_ID) long userId,
                                             @Valid @RequestBody BookingInnerDto bookingInnerDto) {
        logRequest(HttpMethod.POST, "/bookings",
                HEADER_CALLER_ID + userId, bookingInnerDto.toString());
        return bookingClient.addBooking(userId, bookingInnerDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBooking(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                @PathVariable long bookingId,
                                                @RequestParam boolean approved) {
        logRequest(HttpMethod.PATCH, String.format("/bookings/%s?approved=%s", bookingId, approved),
                HEADER_CALLER_ID + userId, "no");
        return bookingClient.updateBooking(userId, bookingId, approved);

    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                 @PathVariable Long bookingId) {
        logRequest(HttpMethod.GET, "/bookings/" + bookingId,
                HEADER_CALLER_ID + userId, "no");
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsForBooker(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                       @RequestParam(defaultValue = "ALL") String state,
                                                       @RequestParam(required = false) @PositiveOrZero Integer from,
                                                       @RequestParam(required = false) @Positive Integer size) {
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new ValidationException(String.format("Unknown state: %s", state)));
        logRequest(HttpMethod.GET, String.format("/bookings?state=%s&from=%s&size=%s", state, from, size),
                HEADER_CALLER_ID + userId, "no");
        return bookingClient.getBookingsForBooker(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsForOwner(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                      @RequestParam(defaultValue = "ALL") String state,
                                                      @RequestParam(required = false) @PositiveOrZero Integer from,
                                                      @RequestParam(required = false) @Positive Integer size) {
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new ValidationException(String.format("Unknown state: %s", state)));
        logRequest(HttpMethod.GET, String.format("/bookings/owner?state=%s&from=%s&size=%s", state, from, size),
                HEADER_CALLER_ID + userId, "no");
        return bookingClient.getBookingsForOwner(userId, bookingState, from, size);
    }
}
