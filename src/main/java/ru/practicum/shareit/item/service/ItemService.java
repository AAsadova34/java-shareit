package ru.practicum.shareit.item.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.comment.dto.CommentInnerDto;
import ru.practicum.shareit.item.comment.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemInnerDto;
import ru.practicum.shareit.item.dto.ItemOutLongDto;
import ru.practicum.shareit.item.dto.ItemOutShortDto;

import java.util.List;

public interface ItemService {
    @Transactional
    ItemOutShortDto addItem(long userId, ItemInnerDto itemInnerDto);

    @Transactional
    ItemOutShortDto updateItem(long userId, long itemId, ItemInnerDto itemInnerDto);

    @Transactional(readOnly = true)
    ItemOutLongDto getItemById(long userId, long itemId);

    @Transactional(readOnly = true)
    List<ItemOutLongDto> getItems(long userId);

    @Transactional(readOnly = true)
    List<ItemOutShortDto> findByNameOrDescription(long userId, String text);

    @Transactional
    CommentOutDto addComment(long userId, long itemId, CommentInnerDto commentInnerDto);
}
