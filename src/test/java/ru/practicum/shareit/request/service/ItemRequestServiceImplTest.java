package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestInnerDto;
import ru.practicum.shareit.request.dto.ItemRequestOutLongDto;
import ru.practicum.shareit.request.dto.ItemRequestOutShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void addItemRequest_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        ItemRequestInnerDto itemRequestInnerDto = new ItemRequestInnerDto();
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemRequestService.addItemRequest(userId, itemRequestInnerDto));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void addItemRequest_whenUserFound_thenSaveItemRequest() {
        long userId = 1L;
        LocalDateTime created = LocalDateTime.now();
        ItemRequestInnerDto itemRequestInnerDto = new ItemRequestInnerDto()
                .setDescription("Looking for something");
        User user = new User().setId(userId);
        ItemRequest itemRequest = new ItemRequest()
                .setId(1L)
                .setDescription("Looking for something")
                .setRequestor(user)
                .setCreated(created);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestOutShortDto itemRequestOutShortDto = itemRequestService
                .addItemRequest(userId, itemRequestInnerDto);

        assertThat(itemRequest.getId(), equalTo(itemRequestOutShortDto.getId()));
        assertThat(itemRequest.getDescription(), equalTo(itemRequestOutShortDto.getDescription()));
        assertThat(itemRequest.getCreated(), equalTo(itemRequestOutShortDto.getCreated()));
    }

    @Test
    void getYourItemRequests_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemRequestService.getYourItemRequests(userId));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(itemRequestRepository, never()).findItemRequestByRequestorIdOrderByCreatedDesc(userId);
    }

    @Test
    void getYourItemRequests_whenListYourRequestsIsEmpty_thenReturnEmptyList() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findItemRequestByRequestorIdOrderByCreatedDesc(userId))
                .thenReturn(new ArrayList<>());

        List<ItemRequestOutLongDto> itemRequestOutLongDto = itemRequestService
                .getYourItemRequests(userId);

        assertTrue(itemRequestOutLongDto.isEmpty());
    }

    @Test
    void getYourItemRequests_whenListYourRequestsNotEmpty_thenReturnYourRequests() {
        long userId = 1L;
        long requestId = 2L;
        LocalDateTime created = LocalDateTime.now();
        User user = new User().setId(userId);
        ItemRequest itemRequest = new ItemRequest()
                .setId(requestId)
                .setDescription("Looking for something")
                .setRequestor(user)
                .setCreated(created);
        List<ItemRequest> itemRequests = List.of(itemRequest);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findItemRequestByRequestorIdOrderByCreatedDesc(userId))
                .thenReturn(itemRequests);

        List<ItemRequestOutLongDto> actualListItemRequests = itemRequestService
                .getYourItemRequests(userId);
        ItemRequestOutLongDto actualItemRequest = actualListItemRequests.get(0);

        assertThat(actualListItemRequests, hasSize(1));
        assertThat(itemRequest.getId(), equalTo(actualItemRequest.getId()));
        assertThat(itemRequest.getDescription(), equalTo(actualItemRequest.getDescription()));
        verify(itemRepository, times(1)).findItemByRequestId(requestId);
        assertThat(actualItemRequest.getItems(), empty());
    }

    @Test
    void getItemRequestsFromOthers_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        Integer from = 0;
        Integer size = 1;
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemRequestService.getItemRequestsFromOthers(userId, from, size));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(itemRequestRepository, never())
                .findAllOtherRequests(userId, PageRequest.of(from / size, size));
        verify(itemRequestRepository, never()).findAllOtherRequests(userId);
    }

    @Test
    void getItemRequestsFromOthers_whenFromAndSizeIsNotNull_thenReturnListRequestsLimitedSize() {
        long userId = 1L;
        Integer from = 0;
        Integer size = 1;
        long requestId = 2L;
        LocalDateTime created = LocalDateTime.now();
        User user = new User().setId(userId);
        ItemRequest itemRequest = new ItemRequest()
                .setId(requestId)
                .setDescription("Looking for something")
                .setRequestor(user)
                .setCreated(created);
        List<ItemRequest> itemRequests = List.of(itemRequest);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findAllOtherRequests(userId, PageRequest.of(from / size, size)))
                .thenReturn(itemRequests);

        List<ItemRequestOutLongDto> actualListItemRequests = itemRequestService
                .getItemRequestsFromOthers(userId, from, size);
        ItemRequestOutLongDto actualItemRequest = actualListItemRequests.get(0);

        assertThat(actualListItemRequests, hasSize(1));
        assertThat(itemRequest.getId(), equalTo(actualItemRequest.getId()));
        assertThat(itemRequest.getDescription(), equalTo(actualItemRequest.getDescription()));
        assertThat(itemRequest.getCreated(), equalTo(actualItemRequest.getCreated()));
        verify(itemRequestRepository, never()).findAllOtherRequests(userId);
    }

    @Test
    void getItemRequestsFromOthers_whenFromOrSizeIsNull_thenReturnListRequests() {
        long userId = 1L;
        Integer from = null;
        Integer size = null;
        long requestId = 2L;
        LocalDateTime created = LocalDateTime.now();
        User user = new User().setId(userId);
        ItemRequest itemRequest = new ItemRequest()
                .setId(requestId)
                .setDescription("Looking for something")
                .setRequestor(user)
                .setCreated(created);
        List<ItemRequest> itemRequests = List.of(itemRequest);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findAllOtherRequests(userId)).thenReturn(itemRequests);

        List<ItemRequestOutLongDto> actualListItemRequests = itemRequestService
                .getItemRequestsFromOthers(userId, from, size);
        ItemRequestOutLongDto actualItemRequest = actualListItemRequests.get(0);

        assertThat(actualListItemRequests, hasSize(1));
        assertThat(itemRequest.getId(), equalTo(actualItemRequest.getId()));
        assertThat(itemRequest.getDescription(), equalTo(actualItemRequest.getDescription()));
        assertThat(itemRequest.getCreated(), equalTo(actualItemRequest.getCreated()));
        verify(itemRequestRepository, never())
                .findAllOtherRequests(anyLong(), any(Pageable.class));
    }

    @Test
    void getItemRequestById_whenUserNoyFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long requestId = 2L;
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemRequestService.getItemRequestById(userId, requestId));
        assertThat(String.format("User with id %s not found", userId), equalTo(e.getMessage()));
        verify(itemRequestRepository, never()).getReferenceById(requestId);
    }

    @Test
    void getItemRequestById_whenUserAndItemRequestWithItemsFound_thenReturnItemRequest() {
        long userId = 1L;
        long requestId = 2L;
        long itemId = 3L;
        long itemOwner = 4L;
        LocalDateTime created = LocalDateTime.now();
        User user = new User().setId(userId);
        ItemRequest itemRequest = new ItemRequest()
                .setId(requestId)
                .setDescription("Looking for something")
                .setRequestor(user)
                .setCreated(created);
        Item item = new Item()
                .setId(itemId)
                .setUserId(itemOwner)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestId);
        List<Item> items = List.of(item);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.getReferenceById(requestId)).thenReturn(itemRequest);
        when(itemRepository.findItemByRequestId(requestId)).thenReturn(items);

        ItemRequestOutLongDto actualItemRequest = itemRequestService.getItemRequestById(userId, requestId);
        ItemOutShortDto actualItemOutShortDto = actualItemRequest.getItems().get(0);

        assertThat(itemRequest.getId(), equalTo(actualItemRequest.getId()));
        assertThat(itemRequest.getDescription(), equalTo(actualItemRequest.getDescription()));
        assertThat(item.getId(), equalTo(actualItemOutShortDto.getId()));
        assertThat(item.getName(), equalTo(actualItemOutShortDto.getName()));
        assertThat(item.getDescription(), equalTo(actualItemOutShortDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(actualItemOutShortDto.getAvailable()));
        assertThat(item.getRequestId(), equalTo(actualItemOutShortDto.getRequestId()));
    }
}