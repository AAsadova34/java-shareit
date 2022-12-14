package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.log.Logger.logRequest;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        logRequest(HttpMethod.POST, "/items",
                "X-Sharer-User-Id-" + userId, itemDto.toString());
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId, @Valid @RequestBody ItemDto itemDto) {
        logRequest(HttpMethod.PATCH, "/items/" + itemId,
                "X-Sharer-User-Id-" + userId, itemDto.toString());
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(name = "X-Sharer-User-Id") long userId,
                               @PathVariable long itemId) {
        logRequest(HttpMethod.GET, "/items/" + itemId,
                "X-Sharer-User-Id-" + userId, "no");
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        logRequest(HttpMethod.GET, "/items",
                "X-Sharer-User-Id-" + userId, "no");
        return itemService.getItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findByNameOrDescription(@RequestHeader(name = "X-Sharer-User-Id") long userId,
                                                 @RequestParam String text) {
        logRequest(HttpMethod.GET, "items/search?text=" + text,
                "X-Sharer-User-Id-" + userId, "no");
        return itemService.findByNameOrDescription(userId, text);
    }
}
