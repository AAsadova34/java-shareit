package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentInnerDto;
import ru.practicum.shareit.item.comment.dto.CommentOutDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemInnerDto;
import ru.practicum.shareit.item.dto.ItemOutLongDto;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void addItem_whenUserNotFound_NotFoundExceptionThrow() {
        long userId = 1L;
        ItemInnerDto itemInnerDto = new ItemInnerDto();
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService.addItem(userId, itemInnerDto));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addItem_whenItemInnerDtoNameIsNullOrEmptyOrBlank_thenValidationExceptionThrow(String name) {
        long userId = 1L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName(name)
                .setDescription("Item description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> itemService.addItem(userId, itemInnerDto));
        assertThat("Name must not be null or empty", equalTo(e.getMessage()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addItem_whenItemInnerDtoDescriptionIsNullOrEmptyOrBlank_thenValidationExceptionThrow(String description) {
        long userId = 1L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("Item name")
                .setDescription(description)
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> itemService.addItem(userId, itemInnerDto));
        assertThat("Description must not be null or empty", equalTo(e.getMessage()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @ParameterizedTest
    @NullSource
    void addItem_whenItemInnerDtoAvailableIsNull_thenValidationExceptionThrow(Boolean available) {
        long userId = 1L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(available);
        when(userRepository.existsById(userId)).thenReturn(true);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> itemService.addItem(userId, itemInnerDto));
        assertThat("Available must not be null", equalTo(e.getMessage()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void addItem_whenItemInnerDtoRequestIdNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long requestId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.existsById(requestId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService.addItem(userId, itemInnerDto));
        assertThat(String.format("ItemRequest with id %s not found", requestId), equalTo(e.getMessage()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void addItem_whenItemInnerDtoIsValidAndRequestIdFound_thenSaveItem() {
        long userId = 1L;
        long itemId = 2L;
        long requestId = 3L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestId);
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestId);
        ItemOutShortDto itemOutShortDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.existsById(requestId)).thenReturn(true);
        when(itemRepository.save(any(Item.class))).thenReturn(itemStorage);

        ItemOutShortDto actualItemOutShortDto = itemService.addItem(userId, itemInnerDto);

        assertThat(itemOutShortDto, equalTo(actualItemOutShortDto));
    }

    @Test
    void addItem_whenItemInnerDtoIsValidAndRequestIdIsNull_thenSaveItem() {
        long userId = 1L;
        long itemId = 1L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        ItemOutShortDto itemOutShortDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.save(any(Item.class))).thenReturn(itemStorage);

        ItemOutShortDto actualItemOutShortDto = itemService.addItem(userId, itemInnerDto);

        assertThat(itemOutShortDto, equalTo(actualItemOutShortDto));
    }

    @Test
    void updateItem_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService.updateItem(userId, itemId, itemInnerDto));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenItemNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService.updateItem(userId, itemId, itemInnerDto));
        assertThat(String.format("Item with id %s not found", itemId), equalTo(e.getMessage()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenTheNonOwnerUpdates_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 2L;
        long itemOwnerId = 3;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("newItem name")
                .setDescription("newItem description")
                .setAvailable(false);
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(itemOwnerId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService.updateItem(userId, itemId, itemInnerDto));
        assertThat(String.format("The user with id %s cannot change an item that he does not own", userId),
                equalTo(e.getMessage()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenFilledInName_thenUpdateName() {
        long userId = 1L;
        long itemId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("newItem name");
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        ItemOutShortDto itemOutShortDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("newItem name")
                .setDescription("Item description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);
        when(itemRepository.save(itemStorage)).thenReturn(itemStorage);

        ItemOutShortDto actualItemOutShortDto = itemService.updateItem(userId, itemId, itemInnerDto);

        assertThat(itemOutShortDto, equalTo(actualItemOutShortDto));
    }

    @Test
    void updateItem_whenFilledInDescription_thenUpdateDescription() {
        long userId = 1L;
        long itemId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setDescription("newItem description");
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        ItemOutShortDto itemOutShortDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("newItem description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);
        when(itemRepository.save(itemStorage)).thenReturn(itemStorage);

        ItemOutShortDto actualItemOutShortDto = itemService.updateItem(userId, itemId, itemInnerDto);

        assertThat(itemOutShortDto, equalTo(actualItemOutShortDto));
    }

    @Test
    void updateItem_whenFilledInAvailable_thenUpdateAvailable() {
        long userId = 1L;
        long itemId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setAvailable(false);
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        ItemOutShortDto itemOutShortDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(false);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);
        when(itemRepository.save(itemStorage)).thenReturn(itemStorage);

        ItemOutShortDto actualItemOutShortDto = itemService.updateItem(userId, itemId, itemInnerDto);

        assertThat(itemOutShortDto, equalTo(actualItemOutShortDto));
    }

    @Test
    void updateItem_whenFilledInNameAndDescriptionAndAvailable_thenUpdateNameAndDescriptionAndAvailable() {
        long userId = 1L;
        long itemId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("newItem name")
                .setDescription("newItem description")
                .setAvailable(false);
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        ItemOutShortDto itemOutShortDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("newItem name")
                .setDescription("newItem description")
                .setAvailable(false);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);
        when(itemRepository.save(itemStorage)).thenReturn(itemStorage);

        ItemOutShortDto actualItemOutShortDto = itemService.updateItem(userId, itemId, itemInnerDto);

        assertThat(itemOutShortDto, equalTo(actualItemOutShortDto));
    }

    @Test
    void getItemById_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 2L;
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService.getItemById(userId, itemId));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(itemRepository, never()).getReferenceById(anyLong());
    }

    @Test
    void getItemById_whenItemNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 2L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService.getItemById(userId, itemId));
        assertThat(String.format("Item with id %s not found", itemId), equalTo(e.getMessage()));
        verify(itemRepository, never()).getReferenceById(anyLong());
    }

    @Test
    void getItemById_whenNotTheOwnerRequested_thenReturnTheItemWithoutLastAndNextBooking() {
        long userId = 1L;
        long itemId = 2L;
        long itemOwnerId = 3L;
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(itemOwnerId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        ItemOutLongDto itemOutLongDto = new ItemOutLongDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setComments(new ArrayList<>());
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);

        ItemOutLongDto actualItemOutLongDto = itemService.getItemById(userId, itemId);

        assertThat(itemOutLongDto, equalTo(actualItemOutLongDto));
    }

    @Test
    void getItemById_whenThereAreCommentsExists_thenReturnTheItemWithComments() {
        long userId = 1L;
        long itemId = 2L;
        long itemOwnerId = 3L;
        long commentId = 4L;
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(itemOwnerId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        User author = new User()
                .setId(itemOwnerId)
                .setName("User name");
        Comment comment = new Comment()
                .setId(commentId)
                .setText("Comment 1")
                .setItem(itemStorage)
                .setAuthor(author)
                .setCreated(LocalDateTime.now());
        List<Comment> comments = List.of(comment);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);
        when(commentRepository.findAllByItemId(itemStorage.getId())).thenReturn(comments);

        ItemOutLongDto actualItemOutLongDto = itemService.getItemById(userId, itemId);
        List<CommentOutDto> actualComments = actualItemOutLongDto.getComments();
        CommentOutDto actualComment = actualComments.get(0);

        assertThat(actualComments, hasSize(1));
        assertThat(comment.getId(), equalTo(actualComment.getId()));
        assertThat(comment.getText(), equalTo(actualComment.getText()));
        assertThat(author.getName(), equalTo(actualComment.getAuthorName()));
        assertThat(actualComment.getCreated(), notNullValue());
    }

    @Test
    void getItemById_whenTheOwnerRequestedAndBookingsExists_thenReturnTheItemWithLastAndNextBooking() {
        long userId = 1L;
        long itemId = 2L;
        long bookerId = 3L;
        long lastBookingId = 4L;
        long nextBookingId = 5L;
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        User booker = new User()
                .setId(bookerId)
                .setName("User name");
        Booking lastBooking = new Booking()
                .setId(lastBookingId)
                .setItem(itemStorage)
                .setBooker(booker);
        Booking nextBooking = new Booking()
                .setId(nextBookingId)
                .setItem(itemStorage)
                .setBooker(booker);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);
        when(bookingRepository.findLastByItemId(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.ofNullable(lastBooking));
        when(bookingRepository.findNextByItemId(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.ofNullable(nextBooking));

        ItemOutLongDto actualItemOutLongDto = itemService.getItemById(userId, itemId);
        BookingForItemDto actualLastBooking = actualItemOutLongDto.getLastBooking();
        BookingForItemDto actualNextBooking = actualItemOutLongDto.getNextBooking();

        assertThat(lastBooking, notNullValue());
        assertThat(nextBooking, notNullValue());
        assertThat(lastBooking.getId(), equalTo(actualLastBooking.getId()));
        assertThat(nextBooking.getId(), equalTo(actualNextBooking.getId()));
        assertThat(itemStorage.getId(), equalTo(actualLastBooking.getItemId()));
        assertThat(itemStorage.getId(), equalTo(actualNextBooking.getItemId()));
        assertThat(booker.getId(), equalTo(actualLastBooking.getBookerId()));
        assertThat(booker.getId(), equalTo(actualNextBooking.getBookerId()));
    }

    @Test
    void getItemById_whenTheOwnerRequestedAndBookingsNotExists_thenReturnTheItemWithNullBooking() {
        long userId = 1L;
        long itemId = 2L;
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);

        when(bookingRepository.findLastByItemId(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(bookingRepository.findNextByItemId(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        ItemOutLongDto actualItemOutLongDto = itemService.getItemById(userId, itemId);

        assertNull(actualItemOutLongDto.getLastBooking());
        assertNull(actualItemOutLongDto.getNextBooking());
    }

    @Test
    void getItems_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        Integer from = 0;
        Integer size = 1;
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService.getItems(userId, from, size));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(itemRepository, never()).findAllByUserIdOrderById(anyLong());
        verify(itemRepository, never()).findAllByUserIdOrderById(anyLong(), any(Pageable.class));
    }

    @Test
    void getItems_whenFromOrSizeIsNotNull_thenReturnListItemsLimitedSize() {
        long userId = 1L;
        Integer from = 0;
        Integer size = 1;
        long itemId = 2L;
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        List<Item> items = List.of(itemStorage);
        ItemOutLongDto itemOutLongDto = new ItemOutLongDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setComments(new ArrayList<>());
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findAllByUserIdOrderById(userId, PageRequest.of(from / size, size)))
                .thenReturn(items);

        List<ItemOutLongDto> actualItems = itemService.getItems(userId, from, size);

        assertThat(actualItems, hasSize(1));
        assertThat(itemOutLongDto, equalTo(actualItems.get(0)));
        verify(itemRepository, never()).findAllByUserIdOrderById(anyLong());
    }

    @Test
    void getItems_whenFromOrSizeIsNull_thenReturnListItems() {
        long userId = 1L;
        Integer from = null;
        Integer size = null;
        long itemId = 2L;
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        List<Item> items = List.of(itemStorage);
        ItemOutLongDto itemOutLongDto = new ItemOutLongDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setComments(new ArrayList<>());
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findAllByUserIdOrderById(userId)).thenReturn(items);

        List<ItemOutLongDto> actualItems = itemService.getItems(userId, from, size);

        assertThat(actualItems, hasSize(1));
        assertThat(itemOutLongDto, equalTo(actualItems.get(0)));
        verify(itemRepository, never()).findAllByUserIdOrderById(anyLong(), any());
        verify(bookingRepository, times(1))
                .findLastByItemId(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository, times(1))
                .findNextByItemId(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void findByNameOrDescription_whenUserNotFound_thenNotFoundExceptionTrow() {
        long userId = 1L;
        String text = "Some item";
        Integer from = 0;
        Integer size = 1;
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService
                        .findItemsByNameOrDescription(userId, text, from, size));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(itemRepository, never()).findByNameOrDescription(anyString(), anyString(),
                any(Pageable.class));
        verify(itemRepository, never()).findByNameOrDescription(anyString(), anyString());
    }

    @ParameterizedTest
    @EmptySource
    void findByNameOrDescription_whenTextIsNullOrEmpty_thenReturnEmptyList(String text) {
        long userId = 1L;
        Integer from = 0;
        Integer size = 1;
        when(userRepository.existsById(userId)).thenReturn(true);

        List<ItemOutShortDto> actualItems = itemService.findItemsByNameOrDescription(userId, text, from, size);

        assertThat(actualItems, empty());
        verify(itemRepository, never()).findByNameOrDescription(anyString(), anyString(),
                any(Pageable.class));
        verify(itemRepository, never()).findByNameOrDescription(anyString(), anyString());
    }

    @Test
    void findByNameOrDescription_whenFromOrSizeIsNotNull_thenReturnListItemsLimitedSize() {
        long userId = 1L;
        String text = "Some item";
        Integer from = 0;
        Integer size = 1;
        long itemId = 2L;
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        List<Item> items = List.of(itemStorage);
        ItemOutShortDto itemOutShortDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findByNameOrDescription("some item", "some item",
                PageRequest.of(from / size, size))).thenReturn(items);

        List<ItemOutShortDto> actualItems = itemService
                .findItemsByNameOrDescription(userId, text, from, size);

        assertThat(actualItems, hasSize(1));
        assertThat(itemOutShortDto, equalTo(actualItems.get(0)));
        verify(itemRepository, never()).findByNameOrDescription("some item", "some item");
    }

    @Test
    void findByNameOrDescription_whenFromOrSizeIsNull_thenReturnListItems() {
        long userId = 1L;
        String text = "Some item";
        Integer from = null;
        Integer size = null;
        long itemId = 2L;
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(userId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        List<Item> items = List.of(itemStorage);
        ItemOutShortDto itemOutShortDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findByNameOrDescription(anyString(), anyString())).thenReturn(items);

        List<ItemOutShortDto> actualItems = itemService
                .findItemsByNameOrDescription(userId, text, from, size);

        assertThat(actualItems, hasSize(1));
        assertThat(itemOutShortDto, equalTo(actualItems.get(0)));
        verify(itemRepository, never())
                .findByNameOrDescription(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void addComment_whenUserNotFound_NotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 1L;
        CommentInnerDto commentInnerDto = new CommentInnerDto()
                .setText("New comment");
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService
                        .addComment(userId, itemId, commentInnerDto));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void addComment_whenItemNotFound_NotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 1L;
        CommentInnerDto commentInnerDto = new CommentInnerDto()
                .setText("New comment");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemService
                        .addComment(userId, itemId, commentInnerDto));
        assertThat(String.format("Item with id %s not found", itemId), equalTo(e.getMessage()));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void addComment_whenBookingsIsEmpty_ValidationExceptionThrow() {
        long userId = 1L;
        long itemId = 1L;
        CommentInnerDto commentInnerDto = new CommentInnerDto()
                .setText("New comment");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(bookingRepository.findAllByBookerAndFinished(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> itemService
                        .addComment(userId, itemId, commentInnerDto));
        assertThat(String.format("User with id %s did not book the item with id %s " +
                "or the reservation has not ended yet.", userId, itemId), equalTo(e.getMessage()));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void addComment_whenBookingIsEmpty_ValidationExceptionThrow() {
        long userId = 1L;
        long itemId = 2L;
        long itemOwnerId = 3L;
        long commentId = 4L;
        long bookingId = 5L;
        CommentInnerDto commentInnerDto = new CommentInnerDto()
                .setText("New comment");
        User authorStorage = new User()
                .setId(userId)
                .setName("User name");
        Item itemStorage = new Item()
                .setId(itemId)
                .setUserId(itemOwnerId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        Comment comment = new Comment()
                .setId(commentId)
                .setText("New comment")
                .setItem(itemStorage)
                .setAuthor(authorStorage)
                .setCreated(LocalDateTime.now());
        Booking bookingStorage = new Booking()
                .setId(bookingId)
                .setItem(itemStorage)
                .setBooker(authorStorage);
        List<Booking> bookings = List.of(bookingStorage);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(bookingRepository.findAllByBookerAndFinished(anyLong(), anyLong(),
                any(LocalDateTime.class))).thenReturn(bookings);
        when(userRepository.getReferenceById(userId)).thenReturn(authorStorage);
        when(itemRepository.getReferenceById(itemId)).thenReturn(itemStorage);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentOutDto actualComment = itemService.addComment(userId, itemId, commentInnerDto);

        assertThat(comment.getId(), equalTo(actualComment.getId()));
        assertThat(comment.getText(), equalTo(actualComment.getText()));
        assertThat(authorStorage.getName(), equalTo(actualComment.getAuthorName()));
        assertThat(actualComment.getCreated(), notNullValue());
    }
}