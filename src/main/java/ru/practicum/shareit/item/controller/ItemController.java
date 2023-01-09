package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentInnerDto;
import ru.practicum.shareit.item.comment.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemInnerDto;
import ru.practicum.shareit.item.dto.ItemOutLongDto;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.log.Logger.logRequest;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemOutShortDto addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @Valid @RequestBody ItemInnerDto itemInnerDto) {
        logRequest(HttpMethod.POST, "/items",
                "X-Sharer-User-Id-" + userId, itemInnerDto.toString());
        return itemService.addItem(userId, itemInnerDto);
    }

    @PatchMapping("/{itemId}")
    public ItemOutShortDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @PathVariable long itemId, @Valid @RequestBody ItemInnerDto itemInnerDto) {
        logRequest(HttpMethod.PATCH, "/items/" + itemId,
                "X-Sharer-User-Id-" + userId, itemInnerDto.toString());
        return itemService.updateItem(userId, itemId, itemInnerDto);
    }

    @GetMapping("/{itemId}")
    public ItemOutLongDto getItemById(@RequestHeader(name = "X-Sharer-User-Id") long userId,
                                      @PathVariable long itemId) {
        logRequest(HttpMethod.GET, "/items/" + itemId,
                "X-Sharer-User-Id-" + userId, "no");
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemOutLongDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        logRequest(HttpMethod.GET, "/items",
                "X-Sharer-User-Id-" + userId, "no");
        return itemService.getItems(userId);
    }

    @GetMapping("/search")
    public List<ItemOutShortDto> findByNameOrDescription(@RequestHeader(name = "X-Sharer-User-Id") long userId,
                                                         @RequestParam String text) {
        logRequest(HttpMethod.GET, "items/search?text=" + text,
                "X-Sharer-User-Id-" + userId, "no");
        return itemService.findByNameOrDescription(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentOutDto addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId,
                                    @Valid @RequestBody CommentInnerDto commentInnerDto) {
        logRequest(HttpMethod.POST, String.format("/items/%s/comment", itemId),
                "X-Sharer-User-Id-" + userId, commentInnerDto.toString());
        return itemService.addComment(userId, itemId, commentInnerDto);
    }
}
