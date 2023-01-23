package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.booking.dto.BookingInnerDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.consts.ShareItAppConst.HEADER_CALLER_ID;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @SneakyThrows
    @Test
    void addBooking_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        BookingInnerDto bookingInner = new BookingInnerDto();
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, never()).addBooking(anyLong(), any(BookingInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addBooking_whenItemIdIsNull_thenMethodArgumentNotValidExceptionThrow() {
        long bookerId = 1L;
        BookingInnerDto bookingInner = new BookingInnerDto()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(7));
        String exceptionMessage = "Id must not be null";

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .header(HEADER_CALLER_ID, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(bookingService, never()).addBooking(anyLong(), any(BookingInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addBooking_whenItemIdIsNegative_thenMethodArgumentNotValidExceptionThrow() {
        long bookerId = 1L;
        long itemId = -1L;
        BookingInnerDto bookingInner = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(7));
        String exceptionMessage = "Id must not be negative";

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .header(HEADER_CALLER_ID, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(bookingService, never()).addBooking(anyLong(), any(BookingInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addBooking_whenStartIsNull_thenMethodArgumentNotValidExceptionThrow() {
        long bookerId = 1L;
        long itemId = 2L;
        BookingInnerDto bookingInner = new BookingInnerDto()
                .setItemId(itemId)
                .setEnd(LocalDateTime.now().plusDays(7));
        String exceptionMessage = "The start must not be null";

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .header(HEADER_CALLER_ID, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(bookingService, never()).addBooking(anyLong(), any(BookingInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addBooking_whenStartInThePast_thenMethodArgumentNotValidExceptionThrow() {
        long bookerId = 1L;
        long itemId = 2L;
        BookingInnerDto bookingInner = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().minusDays(2))
                .setEnd(LocalDateTime.now().plusDays(7));
        String exceptionMessage = "The start must be in the present or future";

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .header(HEADER_CALLER_ID, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(bookingService, never()).addBooking(anyLong(), any(BookingInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addBooking_whenEndIsNull_thenMethodArgumentNotValidExceptionThrow() {
        long bookerId = 1L;
        long itemId = 2L;
        BookingInnerDto bookingInner = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().plusDays(2));
        String exceptionMessage = "The end must not be null";

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .header(HEADER_CALLER_ID, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(bookingService, never()).addBooking(anyLong(), any(BookingInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addBooking_whenEndNotInTheFuture_thenMethodArgumentNotValidExceptionThrow() {
        long bookerId = 1L;
        long itemId = 2L;
        BookingInnerDto bookingInner = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().minusDays(1));
        String exceptionMessage = "The end must be in the future";

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .header(HEADER_CALLER_ID, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(bookingService, never()).addBooking(anyLong(), any(BookingInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addBooking_whenUserOrItemRequestIsNotFound_thenNotFoundExceptionThrow() {
        long bookerId = 1L;
        long itemId = 2L;
        BookingInnerDto bookingInner = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(7));
        String exceptionMessage = String.format("User with id %s or item with id %s not found",
                bookerId, itemId);
        when(bookingService.addBooking(bookerId, bookingInner)).thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .header(HEADER_CALLER_ID, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, times(1)).addBooking(bookerId, bookingInner);
    }

    @SneakyThrows
    @Test
    void addBooking_whenStartOrEndIsInvalid_thenValidationExceptionThrow() {
        long bookerId = 1L;
        long itemId = 2L;
        BookingInnerDto bookingInner = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(7));
        when(bookingService.addBooking(bookerId, bookingInner)).thenThrow(ValidationException.class);

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .header(HEADER_CALLER_ID, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ValidationException));
        verify(bookingService, times(1)).addBooking(bookerId, bookingInner);
    }

    @SneakyThrows
    @Test
    void addBooking_whenBookingIsValid_thenSaveBooking() {
        long bookerId = 1L;
        long itemId = 2L;
        long bookingId = 3L;
        LocalDateTime start = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(7).truncatedTo(ChronoUnit.SECONDS);
        BookingInnerDto bookingInner = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(start)
                .setEnd(end);
        BookingOutDto bookingOut = new BookingOutDto()
                .setId(bookingId)
                .setItem(new ItemOutShortDto().setId(itemId))
                .setBooker(new UserDto().setId(bookerId))
                .setStart(start)
                .setEnd(end)
                .setStatus(BookingStatus.WAITING);
        when(bookingService.addBooking(bookerId, bookingInner)).thenReturn(bookingOut);

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInner))
                        .header(HEADER_CALLER_ID, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.item", is(bookingOut.getItem()), ItemOutShortDto.class))
                .andExpect(jsonPath("$.booker", is(bookingOut.getBooker()), UserDto.class))
                .andExpect(jsonPath("$.start", is(start.toString())))
                .andExpect(jsonPath("$.end", is(end.toString())))
                .andExpect(jsonPath("$.status", is(bookingOut.getStatus().toString())));
    }

    @SneakyThrows
    @Test
    void updateBooking_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        long bookingId = 1L;
        String approved = "true";
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", approved))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, never()).updateBooking(anyLong(), anyLong(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void updateBooking_whenUserOrItemIsNotFound_thenNotFoundExceptionThrow() {
        long ownerId = 1L;
        long bookingId = 2L;
        String approved = "true";
        when(bookingService.updateBooking(ownerId, bookingId, parseBoolean(approved))).thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(HEADER_CALLER_ID, ownerId)
                        .param("approved", approved))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException));
        verify(bookingService, times(1)).updateBooking(ownerId, bookingId, parseBoolean(approved));
    }

    @SneakyThrows
    @Test
    void updateBooking_whenStatusIsApproved_thenValidationExceptionThrow() {
        long ownerId = 1L;
        long bookingId = 2L;
        String approved = "true";
        String exceptionMessage = String.format("The booking with id %s has already been confirmed", bookingId);
        when(bookingService.updateBooking(ownerId, bookingId, parseBoolean(approved)))
                .thenThrow(new ValidationException(exceptionMessage));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(HEADER_CALLER_ID, ownerId)
                        .param("approved", approved))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ValidationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, times(1)).updateBooking(ownerId, bookingId, parseBoolean(approved));
    }

    @SneakyThrows
    @Test
    void updateBooking_whenRequestIsValid_thenUpdateBooking() {
        long ownerId = 1L;
        long bookingId = 2L;
        String approved = "true";
        LocalDateTime start = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(7).truncatedTo(ChronoUnit.SECONDS);
        BookingOutDto bookingOut = new BookingOutDto()
                .setId(bookingId)
                .setItem(new ItemOutShortDto())
                .setBooker(new UserDto().setId(ownerId))
                .setStart(start)
                .setEnd(end)
                .setStatus(BookingStatus.APPROVED);

        when(bookingService.updateBooking(ownerId, bookingId, parseBoolean(approved)))
                .thenReturn(bookingOut);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(HEADER_CALLER_ID, ownerId)
                        .param("approved", approved))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.item", is(bookingOut.getItem()), ItemOutShortDto.class))
                .andExpect(jsonPath("$.booker", is(bookingOut.getBooker()), UserDto.class))
                .andExpect(jsonPath("$.start", is(start.toString())))
                .andExpect(jsonPath("$.end", is(end.toString())))
                .andExpect(jsonPath("$.status", is(bookingOut.getStatus().toString())));
        verify(bookingService, times(1)).updateBooking(ownerId, bookingId, parseBoolean(approved));
    }

    @SneakyThrows
    @Test
    void getBookingById_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        long bookingId = 1L;
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, never()).getBookingById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void getBookingById_whenUserOrBookingIsNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long bookingId = 2L;
        when(bookingService.getBookingById(userId, bookingId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException));
        verify(bookingService, times(1)).getBookingById(userId, bookingId);
    }

    @SneakyThrows
    @Test
    void getBookingById_whenBookingIsFound_thenReturnBooking() {
        long userId = 1L;
        long bookingId = 2L;
        LocalDateTime start = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(7).truncatedTo(ChronoUnit.SECONDS);
        BookingOutDto bookingOut = new BookingOutDto()
                .setId(bookingId)
                .setItem(new ItemOutShortDto())
                .setBooker(new UserDto().setId(userId))
                .setStart(start)
                .setEnd(end)
                .setStatus(BookingStatus.APPROVED);
        when(bookingService.getBookingById(userId, bookingId)).thenReturn(bookingOut);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.item", is(bookingOut.getItem()), ItemOutShortDto.class))
                .andExpect(jsonPath("$.booker", is(bookingOut.getBooker()), UserDto.class))
                .andExpect(jsonPath("$.start", is(start.toString())))
                .andExpect(jsonPath("$.end", is(end.toString())))
                .andExpect(jsonPath("$.status", is(bookingOut.getStatus().toString())));
    }

    @SneakyThrows
    @Test
    void getBookingsForBooker_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, never()).getBookingsForBooker(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getBookingsForBooker_whenFromIsNegative_thenConstraintViolationExceptionThrow() {
        long userId = 1L;
        String state = "ALL";
        String from = "-1";
        String size = "2";
        String exceptionMessage = "getBookingsForBooker.from: must be greater than or equal to 0";

        mockMvc.perform(get("/bookings")
                        .header(HEADER_CALLER_ID, userId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, never()).getBookingsForBooker(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {"-1", "0"})
    void getBookingsForBooker_whenSizeIsNegative_thenConstraintViolationExceptionThrow(String size) {
        long userId = 1L;
        String state = "ALL";
        String from = "0";
        String exceptionMessage = "getBookingsForBooker.size: must be greater than 0";

        mockMvc.perform(get("/bookings")
                        .header(HEADER_CALLER_ID, userId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, never()).getBookingsForBooker(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getBookingsForBooker_whenUserIsNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        String state = "ALL";
        String from = "0";
        String size = "1";
        String exceptionMessage = String.format("User with id %s not found", userId);
        when(bookingService.getBookingsForBooker(userId, state, parseInt(from), parseInt(size)))
                .thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(get("/bookings")
                        .header(HEADER_CALLER_ID, userId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, times(1))
                .getBookingsForBooker(userId, state, parseInt(from), parseInt(size));
    }

    @SneakyThrows
    @Test
    void getBookingsForBooker_whenFromAndSizeIsNotNull_thenReturnLimitedList() {
        long userId = 1L;
        String state = "ALL";
        String from = "0";
        String size = "1";
        long bookingId = 2L;
        LocalDateTime start = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(7).truncatedTo(ChronoUnit.SECONDS);
        BookingOutDto bookingOut = new BookingOutDto()
                .setId(bookingId)
                .setItem(new ItemOutShortDto())
                .setBooker(new UserDto().setId(userId))
                .setStart(start)
                .setEnd(end)
                .setStatus(BookingStatus.APPROVED);
        List<BookingOutDto> bookings = List.of(bookingOut);
        when(bookingService.getBookingsForBooker(userId, state, parseInt(from), parseInt(size)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header(HEADER_CALLER_ID, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingId), Long.class))
                .andExpect(jsonPath("$[0].item", is(bookingOut.getItem()), ItemOutShortDto.class))
                .andExpect(jsonPath("$[0].booker", is(bookingOut.getBooker()), UserDto.class))
                .andExpect(jsonPath("$[0].start", is(start.toString())))
                .andExpect(jsonPath("$[0].end", is(end.toString())))
                .andExpect(jsonPath("$[0].status", is(bookingOut.getStatus().toString())));
    }

    @SneakyThrows
    @Test
    void getBookingsForBooker_whenFromAndSizeIsNull_thenReturnFullList() {
        long userId = 1L;
        String state = "CURRENT";
        long bookingId = 2L;
        LocalDateTime start = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(7).truncatedTo(ChronoUnit.SECONDS);
        BookingOutDto bookingOut = new BookingOutDto()
                .setId(bookingId)
                .setItem(new ItemOutShortDto())
                .setBooker(new UserDto().setId(userId))
                .setStart(start)
                .setEnd(end)
                .setStatus(BookingStatus.APPROVED);
        List<BookingOutDto> bookings = List.of(bookingOut);
        when(bookingService.getBookingsForBooker(userId, state, null, null)).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header(HEADER_CALLER_ID, userId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingId), Long.class))
                .andExpect(jsonPath("$[0].item", is(bookingOut.getItem()), ItemOutShortDto.class))
                .andExpect(jsonPath("$[0].booker", is(bookingOut.getBooker()), UserDto.class))
                .andExpect(jsonPath("$[0].start", is(start.toString())))
                .andExpect(jsonPath("$[0].end", is(end.toString())))
                .andExpect(jsonPath("$[0].status", is(bookingOut.getStatus().toString())));
    }

    @SneakyThrows
    @Test
    void getBookingsForOwner_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(get("/bookings/owner"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, never()).getBookingsForOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getBookingsForOwner_whenFromIsNegative_thenConstraintViolationExceptionThrow() {
        long userId = 1L;
        String state = "ALL";
        String from = "-1";
        String size = "2";
        String exceptionMessage = "getBookingsForOwner.from: must be greater than or equal to 0";

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_CALLER_ID, userId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, never()).getBookingsForOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {"-1", "0"})
    void getBookingsForOwner_whenSizeIsNegative_thenConstraintViolationExceptionThrow(String size) {
        long userId = 1L;
        String state = "ALL";
        String from = "0";
        String exceptionMessage = "getBookingsForOwner.size: must be greater than 0";

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_CALLER_ID, userId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, never()).getBookingsForOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getBookingsForOwner_whenUserIsNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        String state = "ALL";
        String from = "0";
        String size = "1";
        String exceptionMessage = String.format("User with id %s not found", userId);
        when(bookingService.getBookingsForOwner(userId, state, parseInt(from), parseInt(size)))
                .thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_CALLER_ID, userId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(bookingService, times(1))
                .getBookingsForOwner(userId, state, parseInt(from), parseInt(size));
    }

    @SneakyThrows
    @Test
    void getBookingsForOwner_whenFromAndSizeIsNotNull_thenReturnLimitedList() {
        long userId = 1L;
        String state = "ALL";
        String from = "0";
        String size = "1";
        long bookingId = 2L;
        LocalDateTime start = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(7).truncatedTo(ChronoUnit.SECONDS);
        BookingOutDto bookingOut = new BookingOutDto()
                .setId(bookingId)
                .setItem(new ItemOutShortDto())
                .setBooker(new UserDto().setId(userId))
                .setStart(start)
                .setEnd(end)
                .setStatus(BookingStatus.APPROVED);
        List<BookingOutDto> bookings = List.of(bookingOut);
        when(bookingService.getBookingsForOwner(userId, state, parseInt(from), parseInt(size)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_CALLER_ID, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingId), Long.class))
                .andExpect(jsonPath("$[0].item", is(bookingOut.getItem()), ItemOutShortDto.class))
                .andExpect(jsonPath("$[0].booker", is(bookingOut.getBooker()), UserDto.class))
                .andExpect(jsonPath("$[0].start", is(start.toString())))
                .andExpect(jsonPath("$[0].end", is(end.toString())))
                .andExpect(jsonPath("$[0].status", is(bookingOut.getStatus().toString())));
    }

    @SneakyThrows
    @Test
    void getBookingsForOwner_whenFromAndSizeIsNull_thenReturnFullList() {
        long userId = 1L;
        String state = "CURRENT";
        long bookingId = 2L;
        LocalDateTime start = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(7).truncatedTo(ChronoUnit.SECONDS);
        BookingOutDto bookingOut = new BookingOutDto()
                .setId(bookingId)
                .setItem(new ItemOutShortDto())
                .setBooker(new UserDto().setId(userId))
                .setStart(start)
                .setEnd(end)
                .setStatus(BookingStatus.APPROVED);
        List<BookingOutDto> bookings = List.of(bookingOut);
        when(bookingService.getBookingsForOwner(userId, state, null, null)).thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_CALLER_ID, userId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingId), Long.class))
                .andExpect(jsonPath("$[0].item", is(bookingOut.getItem()), ItemOutShortDto.class))
                .andExpect(jsonPath("$[0].booker", is(bookingOut.getBooker()), UserDto.class))
                .andExpect(jsonPath("$[0].start", is(start.toString())))
                .andExpect(jsonPath("$[0].end", is(end.toString())))
                .andExpect(jsonPath("$[0].status", is(bookingOut.getStatus().toString())));
    }

    private String getMessageForMethodArgumentNotValidException(Exception e) {
        String message = "";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException eValidation = (MethodArgumentNotValidException) e;
            message = Objects.requireNonNull(eValidation.getBindingResult().getFieldError()).getDefaultMessage();
        }
        return message;
    }

}