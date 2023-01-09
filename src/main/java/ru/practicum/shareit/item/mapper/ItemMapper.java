package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.comment.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemInnerDto;
import ru.practicum.shareit.item.dto.ItemOutLongDto;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@UtilityClass
public class ItemMapper {
    public static Item toItem(Long userId, ItemInnerDto itemInnerDto) {
        Item item = new Item();
        item.setUserId(userId);
        item.setName(itemInnerDto.getName());
        item.setDescription(itemInnerDto.getDescription());
        item.setAvailable(itemInnerDto.getAvailable());
        return item;
    }

    public static ItemOutShortDto toItemOutShortDto(Long itemId, Item item) {
        ItemOutShortDto itemOutShortDto = new ItemOutShortDto();
        itemOutShortDto.setId(itemId);
        itemOutShortDto.setName(item.getName());
        itemOutShortDto.setDescription(item.getDescription());
        itemOutShortDto.setAvailable(item.getAvailable());
        return itemOutShortDto;
    }

    public static ItemOutLongDto toItemOutLongDto(Item item, BookingForItemDto lastBooking,
                                                  BookingForItemDto nextBooking, List<CommentOutDto> comments) {
        ItemOutLongDto itemOutLongDto = new ItemOutLongDto();
        itemOutLongDto.setId(item.getId());
        itemOutLongDto.setName(item.getName());
        itemOutLongDto.setDescription(item.getDescription());
        itemOutLongDto.setAvailable(item.getAvailable());
        itemOutLongDto.setLastBooking(lastBooking);
        itemOutLongDto.setNextBooking(nextBooking);
        itemOutLongDto.setComments(comments);
        return itemOutLongDto;
    }
}
