package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingInnerDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addBooking(long userId, BookingInnerDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> updateBooking(long userId, long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of(
                "approved", approved
        );
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookingsForBooker(long userId, BookingState state,
                                                       Integer from, Integer size) {
        Map<String, Object> parameters = getParameters(state, from, size);
        if (parameters.containsKey("from") && parameters.containsKey("size")) {
            return get("?state={state}&from={from}&size={size}", userId, parameters);

        } else {
            return get("?state={state}", userId, parameters);
        }
    }

    public ResponseEntity<Object> getBookingsForOwner(long userId, BookingState state,
                                                      Integer from, Integer size) {
        Map<String, Object> parameters = getParameters(state, from, size);
        if (parameters.containsKey("from") && parameters.containsKey("size")) {
            return get("/owner?state={state}&from={from}&size={size}", userId, parameters);

        } else {
            return get("/owner?state={state}", userId, parameters);
        }
    }

    private Map<String, Object> getParameters(BookingState state, Integer from, Integer size){
        Map<String, Object> parameters;
        if (from == null || size == null) {
            parameters = Map.of(
                    "state", state.name()
            );
        } else {
            parameters = Map.of(
                    "state", state.name(),
                    "from", from,
                    "size", size
            );
        }
        return parameters;
    }
}
