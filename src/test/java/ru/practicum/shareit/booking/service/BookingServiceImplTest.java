package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingInnerDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.enums.BookingStatus.*;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemOutShortDto;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void addBooking_whenUserNotFound_thenNotFoundExceptionThrow() {
        long bookerId = 1L;
        BookingInnerDto bookingInnerDto = new BookingInnerDto();
        when(userRepository.existsById(bookerId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> bookingService.addBooking(bookerId, bookingInnerDto));
        assertThat(String.format("User with id %s not found", bookerId), equalTo(e.getMessage()));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_whenItemNotFound_thenNotFoundExceptionThrow() {
        long bookerId = 1L;
        long itemId = 1L;
        BookingInnerDto bookingInnerDto = new BookingInnerDto()
                .setItemId(itemId);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> bookingService.addBooking(bookerId, bookingInnerDto));
        assertThat(String.format("Item with id %s not found", itemId), equalTo(e.getMessage()));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_whenEndBeforeStart_thenValidationExceptionThrow() {
        long bookerId = 1L;
        long itemId = 1L;
        BookingInnerDto bookingInnerDto = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().plusDays(1))
                .setEnd(LocalDateTime.now().minusDays(5));
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> bookingService.addBooking(bookerId, bookingInnerDto));
        assertThat("The end of the booking should not be before it starts", equalTo(e.getMessage()));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_whenItemNotAvailable_thenValidationExceptionThrow() {
        long bookerId = 1L;
        long itemId = 1L;
        BookingInnerDto bookingInnerDto = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().plusDays(1))
                .setEnd(LocalDateTime.now().plusDays(5));
        Item item = new Item()
                .setId(itemId)
                .setAvailable(false);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById((Long) itemId)).thenReturn(item);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> bookingService.addBooking(bookerId, bookingInnerDto));
        assertThat(String.format("The item with id %s is not available for booking",
                item.getId()), equalTo(e.getMessage()));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_whenBookerIsOwner_thenNotFoundExceptionThrow() {
        long bookerId = 1L;
        long itemId = 1L;
        BookingInnerDto bookingInnerDto = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().plusDays(1))
                .setEnd(LocalDateTime.now().plusDays(5));
        Item item = new Item()
                .setId(itemId)
                .setUserId(bookerId)
                .setAvailable(true);
        User booker = new User()
                .setId(bookerId);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById((Long) itemId)).thenReturn(item);
        when(userRepository.getReferenceById(bookerId)).thenReturn(booker);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> bookingService.addBooking(bookerId, bookingInnerDto));
        assertThat("It is impossible to book a thing if you are its owner", equalTo(e.getMessage()));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_whenRequestIsValid_thenSaveBooking() {
        long bookerId = 1L;
        long itemId = 2L;
        long bookingId = 3L;
        BookingInnerDto bookingInnerDto = new BookingInnerDto()
                .setItemId(itemId)
                .setStart(LocalDateTime.now().plusDays(1))
                .setEnd(LocalDateTime.now().plusDays(5));
        Item item = new Item()
                .setId(itemId)
                .setUserId(bookerId + 1)
                .setAvailable(true);
        User booker = new User()
                .setId(bookerId);
        Booking bookingStorage = new Booking()
                .setId(bookingId)
                .setStart(bookingInnerDto.getStart())
                .setEnd(bookingInnerDto.getEnd())
                .setItem(item)
                .setBooker(booker)
                .setStatus(WAITING);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById((Long) itemId)).thenReturn(item);
        when(userRepository.getReferenceById(bookerId)).thenReturn(booker);
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingStorage);

        BookingOutDto bookingOutDto = bookingService.addBooking(bookerId, bookingInnerDto);

        assertThat(bookingStorage.getId(), equalTo(bookingOutDto.getId()));
        assertThat(toItemOutShortDto(itemId, item), equalTo(bookingOutDto.getItem()));
        assertThat(item.getId(), equalTo(bookingOutDto.getItem().getId()));
        assertThat(booker.getId(), equalTo(bookingOutDto.getBooker().getId()));
        assertThat(bookingStorage.getStart(), equalTo(bookingOutDto.getStart()));
        assertThat(bookingStorage.getEnd(), equalTo(bookingOutDto.getEnd()));
        assertThat(bookingStorage.getStatus(), equalTo(bookingOutDto.getStatus()));
    }

    @Test
    void updateBooking_whenUserNotFound_thenNotFoundExceptionThrow() {
        long ownerId = 1L;
        long bookingId = 2L;
        boolean approved = true;
        when(userRepository.existsById(ownerId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> bookingService.updateBooking(ownerId, bookingId, approved));
        assertThat(String.format("User with id %s not found", ownerId), equalTo(e.getMessage()));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_whenTheNonOwnerUpdates_thenNotFoundExceptionThrow() {
        long ownerId = 1L;
        long bookingId = 2L;
        boolean approved = true;
        long bookerId = 3L;
        long realOwner = 4L;
        User booker = new User().setId(bookerId);
        Item item = new Item().setUserId(realOwner);
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(booker)
                .setItem(item);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.getReferenceById(bookingId)).thenReturn(booking);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> bookingService.updateBooking(ownerId, bookingId, approved));
        assertThat(String.format("The user with id %s cannot change an item that he does not own",
                ownerId), equalTo(e.getMessage()));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_whenBookingStatusIsApproved_thenValidationExceptionThrow() {
        long ownerId = 1L;
        long bookingId = 2L;
        boolean approved = true;
        long bookerId = 3L;
        User booker = new User().setId(bookerId);
        Item item = new Item().setUserId(ownerId);
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(booker)
                .setItem(item)
                .setStatus(APPROVED);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.getReferenceById(bookingId)).thenReturn(booking);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> bookingService.updateBooking(ownerId, bookingId, approved));
        assertThat(String.format("The booking with id %s has already been confirmed",
                bookingId), equalTo(e.getMessage()));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_whenApprovedByTheOwner_thenChangeTheStatusToApproved() {
        long ownerId = 1L;
        long bookingId = 2L;
        boolean approved = true;
        long bookerId = 3L;
        User booker = new User().setId(bookerId);
        Item item = new Item().setUserId(ownerId);
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(booker)
                .setItem(item)
                .setStatus(WAITING);
        Booking updateBooking = new Booking()
                .setId(bookingId)
                .setBooker(booker)
                .setItem(item)
                .setStatus(APPROVED);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.getReferenceById(bookingId)).thenReturn(booking);
        when(bookingRepository.save(updateBooking)).thenReturn(updateBooking);

        BookingOutDto bookingOutDto = bookingService.updateBooking(ownerId, bookingId, approved);

        assertThat(APPROVED, equalTo(bookingOutDto.getStatus()));
    }

    @Test
    void updateBooking_whenRejectedByTheOwner_thenChangeTheStatusToRejected() {
        long ownerId = 1L;
        long bookingId = 2L;
        boolean approved = false;
        long bookerId = 3L;
        User booker = new User().setId(bookerId);
        Item item = new Item().setUserId(ownerId);
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(booker)
                .setItem(item)
                .setStatus(WAITING);
        Booking updateBooking = new Booking()
                .setId(bookingId)
                .setBooker(booker)
                .setItem(item)
                .setStatus(REJECTED);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.getReferenceById(bookingId)).thenReturn(booking);
        when(bookingRepository.save(updateBooking)).thenReturn(updateBooking);

        BookingOutDto bookingOutDto = bookingService.updateBooking(ownerId, bookingId, approved);

        assertThat(REJECTED, equalTo(bookingOutDto.getStatus()));
    }

    @Test
    void getBookingById_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long bookingId = 2L;
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> bookingService.getBookingById(userId, bookingId));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(bookingRepository, never()).getReferenceById(bookingId);
    }

    @Test
    void getBookingById_whenRequestsIsNotTheOwnerAndNotTheBooker_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemOwner = 4L;
        User booker = new User().setId(bookerId);
        Item item = new Item().setUserId(itemOwner);
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(booker)
                .setItem(item)
                .setStatus(WAITING);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.getReferenceById(bookingId)).thenReturn(booking);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> bookingService.getBookingById(userId, bookingId));
        assertThat(String.format("The user with id %s is not the owner and not booker",
                userId), equalTo(e.getMessage()));
    }

    @Test
    void getBookingById_whenRequestsIsTheOwner_thenReturnBooking() {
        long userId = 1L;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        User booker = new User().setId(bookerId);
        Item item = new Item()
                .setId(itemId)
                .setUserId(userId);
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(booker)
                .setItem(item)
                .setStart(LocalDateTime.now().plusDays(1))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setStatus(WAITING);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.getReferenceById(bookingId)).thenReturn(booking);

        BookingOutDto bookingOutDto = bookingService.getBookingById(userId, bookingId);

        assertThat(booking.getId(), equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(booker.getId(), equalTo(bookingOutDto.getBooker().getId()));
        assertThat(booking.getStart(), equalTo(bookingOutDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingOutDto.getEnd()));
        assertThat(booking.getStatus(), equalTo(bookingOutDto.getStatus()));
    }

    @Test
    void getBookingById_whenRequestsIsTheBooker_thenReturnBooking() {
        long userId = 1L;
        long bookingId = 2L;
        long itemId = 4L;
        User booker = new User().setId(userId);
        Item item = new Item()
                .setId(itemId)
                .setUserId(userId);
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(booker)
                .setItem(item)
                .setStart(LocalDateTime.now().plusDays(1))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setStatus(WAITING);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.getReferenceById(bookingId)).thenReturn(booking);

        BookingOutDto bookingOutDto = bookingService.getBookingById(userId, bookingId);

        assertThat(booking.getId(), equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(booker.getId(), equalTo(bookingOutDto.getBooker().getId()));
        assertThat(booking.getStart(), equalTo(bookingOutDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingOutDto.getEnd()));
        assertThat(booking.getStatus(), equalTo(bookingOutDto.getStatus()));
    }

    @Test
    void getBookingsForBooker_whenUserNotFound_thenNotFoundExceptionThrow() {
        long bookerId = 1L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 1;
        when(userRepository.existsById(bookerId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> bookingService
                        .getBookingsForBooker(bookerId, state, from, size));
        assertThat(String.format("User with id %s not found", bookerId), equalTo(e.getMessage()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNotNullAndStateIsAll_thenReturnBookingOutDtoList() {
        long bookerId = 1L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, PageRequest.of(from / size, size)))
                .thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WAITING", "REJECTED"})
    void getBookingsForBooker_whenFromOrSizeIsNotNullAndStateIsWaitingOrRejected_thenReturnBookingOutDtoList(String state) {
        long bookerId = 1L;
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.valueOf(state),
                PageRequest.of(from / size, size))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNotNullAndStateIsPast_thenReturnBookingOutDtoList() {
        long bookerId = 1L;
        String state = "PAST";
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNotNullAndStateIsFuture_thenReturnBookingOutDtoList() {
        long bookerId = 1L;
        String state = "FUTURE";
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNotNullAndStateIsCurrent_thenReturnBookingOutDtoList() {
        long bookerId = 1L;
        String state = "CURRENT";
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndCurrent(anyLong(),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNotNullAndStateIsInvalid_thenValidationExceptionThrow() {
        long bookerId = 1L;
        String state = "INVALID";
        Integer from = 0;
        Integer size = 1;
        when(userRepository.existsById(bookerId)).thenReturn(true);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> bookingService.getBookingsForBooker(bookerId, state, from, size));
        assertThat(String.format("Unknown state: %s", state), equalTo(e.getMessage()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNullAndStateIsAll_thenReturnBookingOutDtoList() {
        long bookerId = 1L;
        String state = "ALL";
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId)).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WAITING", "REJECTED"})
    void getBookingsForBooker_whenFromOrSizeIsNullAndStateIsWaitingOrRejected_thenReturnBookingOutDtoList(String state) {
        long bookerId = 1L;
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.valueOf(state)))
                .thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNullAndStateIsPast_thenReturnBookingOutDtoList() {
        long bookerId = 1L;
        String state = "PAST";
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(),
                any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNullAndStateIsFuture_thenReturnBookingOutDtoList() {
        long bookerId = 1L;
        String state = "FUTURE";
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(),
                any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNullAndStateIsCurrent_thenReturnBookingOutDtoList() {
        long bookerId = 1L;
        String state = "CURRENT";
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long itemId = 3L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndCurrent(anyLong(),
                any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForBooker(bookerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForBooker_whenFromOrSizeIsNullAndStateIsInvalid_thenValidationExceptionThrow() {
        long bookerId = 1L;
        String state = "INVALID";
        Integer from = null;
        Integer size = null;
        when(userRepository.existsById(bookerId)).thenReturn(true);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> bookingService.getBookingsForBooker(bookerId, state, from, size));
        assertThat(String.format("Unknown state: %s", state), equalTo(e.getMessage()));
    }

    @Test
    void getBookingsForOwner_whenUserNotFound_thenNotFoundExceptionThrow() {
        long ownerId = 1L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 1;
        when(userRepository.existsById(ownerId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> bookingService
                        .getBookingsForOwner(ownerId, state, from, size));
        assertThat(String.format("User with id %s not found", ownerId), equalTo(e.getMessage()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNotNullAndStateIsAll_thenReturnBookingOutDtoList() {
        long ownerId = 1L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerId(ownerId, PageRequest.of(from / size, size)))
                .thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WAITING", "REJECTED"})
    void getBookingsForOwner_whenFromOrSizeIsNotNullAndStateIsWaitingOrRejected_thenReturnBookingOutDtoList(String state) {
        long ownerId = 1L;
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.valueOf(state),
                PageRequest.of(from / size, size))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNotNullAndStateIsPast_thenReturnBookingOutDtoList() {
        long ownerId = 1L;
        String state = "PAST";
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndPast(anyLong(), any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNotNullAndStateIsFuture_thenReturnBookingOutDtoList() {
        long ownerId = 1L;
        String state = "FUTURE";
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndFuture(anyLong(),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNotNullAndStateIsCurrent_thenReturnBookingOutDtoList() {
        long ownerId = 1L;
        String state = "CURRENT";
        Integer from = 0;
        Integer size = 1;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndCurrent(anyLong(), any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNotNullAndStateIsInvalid_thenValidationExceptionThrow() {
        long ownerId = 1L;
        String state = "INVALID";
        Integer from = 0;
        Integer size = 1;
        when(userRepository.existsById(ownerId)).thenReturn(true);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> bookingService.getBookingsForOwner(ownerId, state, from, size));
        assertThat(String.format("Unknown state: %s", state), equalTo(e.getMessage()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNullAndStateIsAll_thenReturnBookingOutDtoList() {
        long ownerId = 1L;
        String state = "ALL";
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerId(ownerId)).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WAITING", "REJECTED"})
    void getBookingsForOwner_whenFromOrSizeIsNullAndStateIsWaitingOrRejected_thenReturnBookingOutDtoList(String state) {
        long ownerId = 1L;
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.valueOf(state))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNullAndStateIsPast_thenReturnBookingOutDtoList() {
        long ownerId = 1L;
        String state = "PAST";
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndPast(anyLong(), any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNullAndStateIsFuture_thenReturnBookingOutDtoList() {
        long ownerId = 1L;
        String state = "FUTURE";
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndFuture(anyLong(), any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNullAndStateIsCurrent_thenReturnBookingOutDtoList() {
        long ownerId = 1L;
        String state = "CURRENT";
        Integer from = null;
        Integer size = null;
        long bookingId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        Booking booking = new Booking()
                .setId(bookingId)
                .setBooker(new User().setId(bookerId))
                .setItem(new Item().setId(itemId).setUserId(ownerId));
        List<Booking> bookings = List.of(booking);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndCurrent(anyLong(), any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingOutDto> bookingOutDtoList = bookingService.getBookingsForOwner(ownerId, state, from, size);
        BookingOutDto bookingOutDto = bookingOutDtoList.get(0);

        assertThat(bookingId, equalTo(bookingOutDto.getId()));
        assertThat(itemId, equalTo(bookingOutDto.getItem().getId()));
        assertThat(bookerId, equalTo(bookingOutDto.getBooker().getId()));
    }

    @Test
    void getBookingsForOwner_whenFromOrSizeIsNullAndStateIsInvalid_thenValidationExceptionThrow() {
        long ownerId = 1L;
        String state = "INVALID";
        Integer from = null;
        Integer size = null;
        when(userRepository.existsById(ownerId)).thenReturn(true);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> bookingService.getBookingsForOwner(ownerId, state, from, size));
        assertThat(String.format("Unknown state: %s", state), equalTo(e.getMessage()));
    }
}