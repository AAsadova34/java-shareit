package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.booking.enums.BookingStatus.REJECTED;
import static ru.practicum.shareit.booking.enums.BookingStatus.WAITING;

@DataJpaTest()
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User bookerStorage;
    private User ownerStorage;
    private Item itemStorage;

    @BeforeEach
    void saveData() {
        User booker = new User()
                .setName("Booker name")
                .setEmail("booker@yandex.ru");
        bookerStorage = userRepository.save(booker);

        User owner = new User()
                .setName("Owner name")
                .setEmail("owner@yandex.ru");
        ownerStorage = userRepository.save(owner);

        Item newItem = new Item()
                .setUserId(ownerStorage.getId())
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        itemStorage = itemRepository.save(newItem);
    }

    @Test
    void save_whenBookingIsValid_thenSaveBooking() {
        Booking newBooking = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage = bookingRepository.save(newBooking);

        assertThat(bookingStorage.getId(), notNullValue());
        assertThat(newBooking.getStart(), equalTo(bookingStorage.getStart()));
        assertThat(newBooking.getEnd(), equalTo(bookingStorage.getEnd()));
        assertThat(newBooking.getItem(), equalTo(bookingStorage.getItem()));
        assertThat(newBooking.getBooker(), equalTo(bookingStorage.getBooker()));
        assertThat(newBooking.getStatus(), equalTo(bookingStorage.getStatus()));
    }

    @Test
    void save_whenItemNotFound_thenDataIntegrityViolationExceptionThrow() {
        Booking newBooking = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(new Item().setId(-1L))
                .setBooker(bookerStorage)
                .setStatus(WAITING);

        DataIntegrityViolationException e = Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> bookingRepository.save(newBooking));
    }

    @Test
    void save_whenBookerNotFound_thenDataIntegrityViolationExceptionThrow() {
        Booking newBooking = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(new User().setId(-1L))
                .setStatus(WAITING);

        DataIntegrityViolationException e = Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> bookingRepository.save(newBooking));
    }

    @Test
    void getReferenceById_whenBookingIsFound_thenReturnBooking() {
        Booking newBooking = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage = bookingRepository.save(newBooking);

        Booking actualBooking = bookingRepository.getReferenceById(bookingStorage.getId());

        assertThat(bookingStorage.getId(), equalTo(actualBooking.getId()));
        assertThat(bookingStorage.getStart(), equalTo(actualBooking.getStart()));
        assertThat(bookingStorage.getEnd(), equalTo(actualBooking.getEnd()));
        assertThat(bookingStorage.getItem(), equalTo(actualBooking.getItem()));
        assertThat(bookingStorage.getBooker(), equalTo(actualBooking.getBooker()));
        assertThat(bookingStorage.getStatus(), equalTo(actualBooking.getStatus()));
    }

    @Test
    void findAllByBookerIdOrderByStartDesc_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdOrderByStartDesc(bookerStorage.getId(), pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findAllByBookerIdOrderByStartDesc_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdOrderByStartDesc(bookerStorage.getId());

        assertThat(bookings, hasSize(2));
        assertTrue(bookings.get(0).getStart().isAfter(bookings.get(1).getStart()));
    }

    @Test
    void findAllByBookerIdAndStatusOrderByStartDesc_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndStatusOrderByStartDesc(bookerStorage.getId(), WAITING, pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findAllByBookerIdAndStatusOrderByStartDesc_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(REJECTED);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndStatusOrderByStartDesc(bookerStorage.getId(), REJECTED);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage1));
    }

    @Test
    void findAllByBookerIdAndEndBeforeOrderByStartDesc_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusDays(1))
                .setEnd(LocalDateTime.now().minusMinutes(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerStorage.getId(), LocalDateTime.now(), pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findAllByBookerIdAndEndBeforeOrderByStartDesc_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusDays(1))
                .setEnd(LocalDateTime.now().minusMinutes(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerStorage.getId(), LocalDateTime.now());

        assertThat(bookings, hasSize(2));
        assertTrue(bookings.get(0).getStart().isAfter(bookings.get(1).getStart()));
    }

    @Test
    void findAllByBookerIdAndStartAfterOrderByStartDesc_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndStartAfterOrderByStartDesc(bookerStorage.getId(), LocalDateTime.now(), pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage3));
    }

    @Test
    void findAllByBookerIdAndStartAfterOrderByStartDesc_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndStartAfterOrderByStartDesc(bookerStorage.getId(), LocalDateTime.now());

        assertThat(bookings, hasSize(2));
        assertTrue(bookings.get(0).getStart().isAfter(bookings.get(1).getStart()));
    }

    @Test
    void findAllByBookerIdAndCurrent_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndCurrent(bookerStorage.getId(), LocalDateTime.now(), pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findAllByBookerIdAndCurrent_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndCurrent(bookerStorage.getId(), LocalDateTime.now());

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findAllByOwnerId_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerId(ownerStorage.getId(), pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findAllByOwnerId_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerId(ownerStorage.getId());

        assertThat(bookings, hasSize(2));
        assertTrue(bookings.get(0).getStart().isAfter(bookings.get(1).getStart()));
    }

    @Test
    void findAllByOwnerIdAndStatus_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerIdAndStatus(ownerStorage.getId(), WAITING, pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findAllByOwnerIdAndStatus_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(REJECTED);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerIdAndStatus(ownerStorage.getId(), REJECTED);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage1));
    }

    @Test
    void findAllByOwnerIdAndPast_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusDays(1))
                .setEnd(LocalDateTime.now())
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerIdAndPast(ownerStorage.getId(), LocalDateTime.now(), pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findAllByOwnerIdAndPast_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusDays(1))
                .setEnd(LocalDateTime.now())
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerIdAndPast(ownerStorage.getId(), LocalDateTime.now());

        assertThat(bookings, hasSize(2));
        assertTrue(bookings.get(0).getStart().isAfter(bookings.get(1).getStart()));
    }

    @Test
    void findAllByOwnerIdAndFuture_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerIdAndFuture(ownerStorage.getId(), LocalDateTime.now(), pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage3));
    }

    @Test
    void findAllByOwnerIdAndFuture_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().plusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerIdAndFuture(ownerStorage.getId(), LocalDateTime.now());

        assertThat(bookings, hasSize(2));
        assertTrue(bookings.get(0).getStart().isAfter(bookings.get(1).getStart()));
    }

    @Test
    void findAllByOwnerIdAndCurrent_whenPageableIsSet_thenReturnLimitedList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerIdAndCurrent(ownerStorage.getId(), LocalDateTime.now(), pageable);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findAllByOwnerIdAndCurrent_whenPageableIsNotSet_thenReturnFullList() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);

        List<Booking> bookings = bookingRepository
                .findAllByOwnerIdAndCurrent(ownerStorage.getId(), LocalDateTime.now());

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage2));
    }

    @Test
    void findLastByItemId_whenLastBookingIsFound_thenReturnBooking() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);

        Optional<Booking> booking = bookingRepository
                .findLastByItemId(itemStorage.getId(), LocalDateTime.now());

        assertThat(bookingStorage2, equalTo(booking.get()));
    }

    @Test
    void findNextByItemId_whenNextBookingIsFound_thenReturnBooking() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);

        Optional<Booking> booking = bookingRepository
                .findNextByItemId(itemStorage.getId(), LocalDateTime.now());

        assertThat(bookingStorage3, equalTo(booking.get()));
    }

    @Test
    void findAllByBookerAndFinished_whenBookingsAreFound_thenReturnBookings() {
        Booking newBooking1 = new Booking()
                .setStart(LocalDateTime.now().minusDays(5))
                .setEnd(LocalDateTime.now().minusDays(2))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking2 = new Booking()
                .setStart(LocalDateTime.now().minusMinutes(5))
                .setEnd(LocalDateTime.now().plusDays(1))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking newBooking3 = new Booking()
                .setStart(LocalDateTime.now().plusDays(2))
                .setEnd(LocalDateTime.now().plusDays(5))
                .setItem(itemStorage)
                .setBooker(bookerStorage)
                .setStatus(WAITING);
        Booking bookingStorage1 = bookingRepository.save(newBooking1);
        Booking bookingStorage2 = bookingRepository.save(newBooking2);
        Booking bookingStorage3 = bookingRepository.save(newBooking3);

        List<Booking> bookings = bookingRepository
                .findAllByBookerAndFinished(itemStorage.getId(), bookerStorage.getId(), LocalDateTime.now());

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(bookingStorage1));
    }
}