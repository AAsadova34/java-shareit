package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.toItemOutShortDto;
import static ru.practicum.shareit.log.Logger.logStorageChanges;
import static ru.practicum.shareit.request.mapper.ItemRequestMapper.*;
import static ru.practicum.shareit.validation.Validation.checkUserExists;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestOutShortDto addItemRequest(long userId, ItemRequestInnerDto itemRequestInnerDto) {
        checkUserExists(userRepository, userId);
        User requestor = userRepository.getReferenceById(userId);
        ItemRequest itemRequest = toItemRequest(requestor, itemRequestInnerDto);
        ItemRequest itemRequestStorage = itemRequestRepository.save(itemRequest);
        logStorageChanges("Add item request", itemRequestStorage.toString());
        return toItemRequestOutShortDto(itemRequestStorage);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestOutLongDto> getYourItemRequests(long userId) {
        checkUserExists(userRepository, userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findItemRequestByRequestorIdOrderByCreatedDesc(userId);
        return itemRequests.stream()
                .map(itemRequest -> toItemRequestOutLongDto(getItemOutShortDtoList(itemRequest), itemRequest))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestOutLongDto> getItemRequestsFromOthers(long userId, Integer from, Integer size) {
        checkUserExists(userRepository, userId);
        List<ItemRequest> itemRequests;
        if (from != null && size != null) {
            Pageable pageable = PageRequest.of(from / size, size);
            itemRequests = itemRequestRepository.findAllOtherRequests(userId, pageable);
        } else {
            itemRequests = itemRequestRepository.findAllOtherRequests(userId);
        }
        return itemRequests.stream()
                .map(itemRequest -> toItemRequestOutLongDto(getItemOutShortDtoList(itemRequest), itemRequest))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestOutLongDto getItemRequestById(long userId, long requestId) {
        checkUserExists(userRepository, userId);
        ItemRequest itemRequest = itemRequestRepository.getReferenceById(requestId);
        return toItemRequestOutLongDto(getItemOutShortDtoList(itemRequest), itemRequest);
    }

    private List<ItemOutShortDto> getItemOutShortDtoList(ItemRequest itemRequest) {
        List<Item> items = itemRepository.findItemByRequestId(itemRequest.getId());
        if (items.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(item -> toItemOutShortDto(item.getId(), item))
                .collect(Collectors.toList());
    }
}
