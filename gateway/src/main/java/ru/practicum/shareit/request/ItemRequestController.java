package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.consts.ShareItAppConst.HEADER_CALLER_ID;
import static ru.practicum.shareit.log.Logger.logRequest;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                 @Valid @RequestBody ItemRequestInnerDto itemRequestInnerDto) {
        logRequest(HttpMethod.POST, "/requests",
                HEADER_CALLER_ID + userId, itemRequestInnerDto.toString());
        return itemRequestClient.addItemRequest(userId, itemRequestInnerDto);
    }

    @GetMapping
    public ResponseEntity<Object> getYourItemRequests(@RequestHeader(HEADER_CALLER_ID) long userId) {
        logRequest(HttpMethod.GET, "/requests",
                HEADER_CALLER_ID + userId, "no");
        return itemRequestClient.getYourItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getItemRequestsFromOthers(
            @RequestHeader(HEADER_CALLER_ID) long userId,
            @RequestParam(required = false) @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {
        logRequest(HttpMethod.GET, String.format("/requests/all?from=%s&size=%s", from, size),
                HEADER_CALLER_ID + userId, "no");
        return itemRequestClient.getItemRequestsFromOthers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                     @PathVariable long requestId) {
        logRequest(HttpMethod.GET, "/requests/" + requestId,
                HEADER_CALLER_ID + userId, "no");
        return itemRequestClient.getItemRequestById(userId, requestId);
    }
}
