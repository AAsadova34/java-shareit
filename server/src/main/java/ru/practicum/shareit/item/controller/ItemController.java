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

import java.util.List;

import static ru.practicum.shareit.consts.ShareItAppConst.HEADER_CALLER_ID;
import static ru.practicum.shareit.log.Logger.logRequest;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemOutShortDto addItem(@RequestHeader(HEADER_CALLER_ID) long userId,
                                   @RequestBody ItemInnerDto itemInnerDto) {
        logRequest(HttpMethod.POST, "/items",
                HEADER_CALLER_ID + userId, itemInnerDto.toString());
        return itemService.addItem(userId, itemInnerDto);
    }

    @PatchMapping("/{itemId}")
    public ItemOutShortDto updateItem(@RequestHeader(HEADER_CALLER_ID) long userId,
                                      @PathVariable long itemId,
                                      @RequestBody ItemInnerDto itemInnerDto) {
        logRequest(HttpMethod.PATCH, "/items/" + itemId,
                HEADER_CALLER_ID + userId, itemInnerDto.toString());
        return itemService.updateItem(userId, itemId, itemInnerDto);
    }

    @GetMapping("/{itemId}")
    public ItemOutLongDto getItemById(@RequestHeader(HEADER_CALLER_ID) long userId,
                                      @PathVariable long itemId) {
        logRequest(HttpMethod.GET, "/items/" + itemId,
                HEADER_CALLER_ID + userId, "no");
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemOutLongDto> getItems(@RequestHeader(HEADER_CALLER_ID) long userId,
                                         @RequestParam(required = false) Integer from,
                                         @RequestParam(required = false) Integer size) {
        logRequest(HttpMethod.GET, String.format("/items?&from=%s&size=%s", from, size),
                HEADER_CALLER_ID + userId, "no");
        return itemService.getItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemOutShortDto> findItemsByNameOrDescription(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                              @RequestParam String text,
                                                              @RequestParam(required = false) Integer from,
                                                              @RequestParam(required = false) Integer size) {
        logRequest(HttpMethod.GET, String.format("items/search?text=%s&from=%s&size=%s", text, from, size),
                HEADER_CALLER_ID + userId, "no");
        return itemService.findItemsByNameOrDescription(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentOutDto addComment(@RequestHeader(HEADER_CALLER_ID) long userId,
                                    @PathVariable long itemId,
                                    @RequestBody CommentInnerDto commentInnerDto) {
        logRequest(HttpMethod.POST, String.format("/items/%s/comment", itemId),
                HEADER_CALLER_ID + userId, commentInnerDto.toString());
        return itemService.addComment(userId, itemId, commentInnerDto);
    }

}
