package ru.practicum.shareit.request.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestInnerDto;
import ru.practicum.shareit.request.dto.ItemRequestOutLongDto;
import ru.practicum.shareit.request.dto.ItemRequestOutShortDto;

import java.util.List;

public interface ItemRequestService {
    @Transactional
    ItemRequestOutShortDto addItemRequest(long userId, ItemRequestInnerDto itemRequestInnerDto);

    @Transactional(readOnly = true)
    List<ItemRequestOutLongDto> getYourItemRequests(long userId);

    @Transactional(readOnly = true)
    List<ItemRequestOutLongDto> getItemRequestsFromOthers(long userId, Integer from, Integer size);

    @Transactional(readOnly = true)
    ItemRequestOutLongDto getItemRequestById(long userId, long requestId);
}
