package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentInnerDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.consts.ShareItAppConst.HEADER_CALLER_ID;
import static ru.practicum.shareit.log.Logger.logRequest;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(HEADER_CALLER_ID) long userId,
                                          @RequestBody ItemInnerDto itemInnerDto) {
        logRequest(HttpMethod.POST, "/items",
                HEADER_CALLER_ID + userId, itemInnerDto.toString());
        return itemClient.addItem(userId, itemInnerDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(HEADER_CALLER_ID) long userId,
                                             @PathVariable long itemId,
                                             @RequestBody ItemInnerDto itemInnerDto) {
        logRequest(HttpMethod.PATCH, "/items/" + itemId,
                HEADER_CALLER_ID + userId, itemInnerDto.toString());
        return itemClient.updateItem(userId, itemId, itemInnerDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(HEADER_CALLER_ID) long userId,
                                              @PathVariable long itemId) {
        logRequest(HttpMethod.GET, "/items/" + itemId,
                HEADER_CALLER_ID + userId, "no");
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(HEADER_CALLER_ID) long userId,
                                           @RequestParam(required = false) @PositiveOrZero Integer from,
                                           @RequestParam(required = false) @Positive Integer size) {
        logRequest(HttpMethod.GET, String.format("/items?from=%s&size=%s", from, size),
                HEADER_CALLER_ID + userId, "no");
        return itemClient.getItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItemsByNameOrDescription(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                               @RequestParam String text,
                                                               @RequestParam(required = false) @PositiveOrZero Integer from,
                                                               @RequestParam(required = false) @Positive Integer size) {
        logRequest(HttpMethod.GET, String.format("items/search?text=%s&from=%s&size=%s", text, from, size),
                HEADER_CALLER_ID + userId, "no");
        return itemClient.findItemsByNameOrDescription(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(HEADER_CALLER_ID) long userId,
                                             @PathVariable long itemId,
                                             @Valid @RequestBody CommentInnerDto commentInnerDto) {
        logRequest(HttpMethod.POST, String.format("/items/%s/comment", itemId),
                HEADER_CALLER_ID + userId, commentInnerDto.toString());
        return itemClient.addComment(userId, itemId, commentInnerDto);
    }
}
