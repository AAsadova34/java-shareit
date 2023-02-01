package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInnerDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.BookingState;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.enums.BookingStatus.*;
import static ru.practicum.shareit.booking.mapper.BookingMapper.*;

import static ru.practicum.shareit.item.mapper.ItemMapper.toItemOutShortDto;
import static ru.practicum.shareit.log.Logger.logStorageChanges;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;
import static ru.practicum.shareit.validation.Validation.checkItemExists;
import static ru.practicum.shareit.validation.Validation.checkUserExists;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public BookingOutDto addBooking(long bookerId, BookingInnerDto bookingInnerDto) {
        checkUserExists(userRepository, bookerId);
        checkItemExists(itemRepository, bookingInnerDto.getItemId());
        if (bookingInnerDto.getStart().isAfter(bookingInnerDto.getEnd())) {
            throw new ValidationException("The end of the booking should not be before it starts");
        }
        Item item = itemRepository.getReferenceById(bookingInnerDto.getItemId());
        if (!item.getAvailable()) {
            throw new ValidationException(String.format("The item with id %s is not available for booking",
                    item.getId()));
        }
        User booker = userRepository.getReferenceById(bookerId);
        if (booker.getId().equals(item.getUserId())) {
            throw new NotFoundException("It is impossible to book a thing if you are its owner");
        }
        Booking booking = toBooking(bookingInnerDto, item, booker, WAITING);
        Booking bookingStorage = bookingRepository.save(booking);
        logStorageChanges("Add", bookingStorage.toString());
        return toBookingOutDto(bookingStorage, toItemOutShortDto(item.getId(), item), toUserDto(booker.getId(), booker));
    }

    @Transactional
    @Override
    public BookingOutDto updateBooking(long ownerId, long bookingId, boolean approved) {
        checkUserExists(userRepository, ownerId);
        Booking booking = bookingRepository.getReferenceById(bookingId);
        User booker = booking.getBooker();
        Item item = booking.getItem();
        if (ownerId != item.getUserId()) {
            throw new NotFoundException(String.format("The user with id %s cannot change an item that he does not own",
                    ownerId));
        }
        if (booking.getStatus() == APPROVED) {
            throw new ValidationException(String.format("The booking with id %s has already been confirmed",
                    bookingId));
        }
        if (approved) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }
        Booking bookingStorage = bookingRepository.save(booking);
        logStorageChanges("Update", bookingStorage.toString());
        return toBookingOutDto(bookingStorage, toItemOutShortDto(item.getId(), item), toUserDto(booker.getId(), booker));
    }

    @Transactional(readOnly = true)
    @Override
    public BookingOutDto getBookingById(long userId, long bookingId) {
        checkUserExists(userRepository, userId);
        Booking booking = bookingRepository.getReferenceById(bookingId);
        User booker = booking.getBooker();
        Item item = booking.getItem();
        if (booker.getId().equals(userId) ||
                item.getUserId().equals(userId)) {
            return toBookingOutDto(booking, toItemOutShortDto(item.getId(), item), toUserDto(booker.getId(), booker));
        }
        throw new NotFoundException(String.format("The user with id %s is not the owner and not booker",
                userId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutDto> getBookingsForBooker(long bookerId, String state, Integer from, Integer size) {
        checkUserExists(userRepository, bookerId);
        List<Booking> bookings;
        if (from != null && size != null) {
            bookings = getBookingsForBookerWithPagination(bookerId, state, from, size);
        } else {
            bookings = getBookingsForBookerWithoutPagination(bookerId, state);
        }
        return toListBookingOutDto(bookings);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutDto> getBookingsForOwner(long ownerId, String state, Integer from, Integer size) {
        checkUserExists(userRepository, ownerId);
        List<Booking> bookings;
        if (from != null && size != null) {
            bookings = getBookingsForOwnerWithPagination(ownerId, state, from, size);
        } else {
            bookings = getBookingsForOwnerWithoutPagination(ownerId, state);
        }
        return toListBookingOutDto(bookings);
    }

    private List<Booking> getBookingsForBookerWithPagination(long bookerId, String state, Integer from, Integer size) {
        List<Booking> bookings = new ArrayList<>();
        Pageable pageable = PageRequest.of(from / size, size);
        try {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, pageable);
                    break;
                case WAITING:
                case REJECTED:
                    bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId,
                            BookingStatus.valueOf(state), pageable);
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId,
                            LocalDateTime.now(), pageable);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId,
                            LocalDateTime.now(), pageable);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByBookerIdAndCurrent(bookerId, LocalDateTime.now(), pageable);
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", state));
        }
        return bookings;
    }

    private List<Booking> getBookingsForBookerWithoutPagination(long bookerId, String state) {
        List<Booking> bookings = new ArrayList<>();
        try {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId);
                    break;
                case WAITING:
                case REJECTED:
                    bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId,
                            BookingStatus.valueOf(state));
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId,
                            LocalDateTime.now());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId,
                            LocalDateTime.now());
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByBookerIdAndCurrent(bookerId, LocalDateTime.now());
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", state));
        }
        return bookings;
    }

    private List<Booking> getBookingsForOwnerWithPagination(long ownerId, String state, Integer from, Integer size) {
        List<Booking> bookings = new ArrayList<>();
        Pageable pageable = PageRequest.of(from / size, size);
        try {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingRepository.findAllByOwnerId(ownerId, pageable);
                    break;
                case WAITING:
                case REJECTED:
                    bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.valueOf(state),
                            pageable);
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByOwnerIdAndPast(ownerId, LocalDateTime.now(), pageable);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByOwnerIdAndFuture(ownerId, LocalDateTime.now(), pageable);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByOwnerIdAndCurrent(ownerId, LocalDateTime.now(), pageable);
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", state));
        }
        return bookings;
    }

    private List<Booking> getBookingsForOwnerWithoutPagination(long ownerId, String state) {
        List<Booking> bookings = new ArrayList<>();
        try {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingRepository.findAllByOwnerId(ownerId);
                    break;
                case WAITING:
                case REJECTED:
                    bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.valueOf(state));
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByOwnerIdAndPast(ownerId, LocalDateTime.now());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByOwnerIdAndFuture(ownerId, LocalDateTime.now());
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByOwnerIdAndCurrent(ownerId, LocalDateTime.now());
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", state));
        }
        return bookings;
    }

    private List<BookingOutDto> toListBookingOutDto(List<Booking> bookings) {
        return bookings.stream()
                .map(b -> toBookingOutDto(b,
                        toItemOutShortDto(b.getItem().getId(), b.getItem()),
                        toUserDto(b.getBooker().getId(), b.getBooker())))
                .collect(Collectors.toList());
    }
}

