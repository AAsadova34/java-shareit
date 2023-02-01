package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.request.dto.ItemRequestInnerDto;
import ru.practicum.shareit.request.dto.ItemRequestOutLongDto;
import ru.practicum.shareit.request.dto.ItemRequestOutShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@UtilityClass
public class ItemRequestMapper {

    public static ItemRequest toItemRequest(User requestor, ItemRequestInnerDto itemRequestInnerDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestInnerDto.getDescription());
        itemRequest.setRequestor(requestor);
        return itemRequest;
    }

    public static ItemRequestOutShortDto toItemRequestOutShortDto(ItemRequest itemRequest) {
        ItemRequestOutShortDto itemRequestOutShortDto = new ItemRequestOutShortDto();
        itemRequestOutShortDto.setId(itemRequest.getId());
        itemRequestOutShortDto.setDescription(itemRequest.getDescription());
        itemRequestOutShortDto.setCreated(itemRequest.getCreated());
        return itemRequestOutShortDto;
    }

    public static ItemRequestOutLongDto toItemRequestOutLongDto(List<ItemOutShortDto> items, ItemRequest itemRequest) {
        ItemRequestOutLongDto itemRequestOutLongDto = new ItemRequestOutLongDto();
        itemRequestOutLongDto.setId(itemRequest.getId());
        itemRequestOutLongDto.setDescription(itemRequest.getDescription());
        itemRequestOutLongDto.setCreated(itemRequest.getCreated());
        itemRequestOutLongDto.setItems(items);
        return itemRequestOutLongDto;
    }

}
