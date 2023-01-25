package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestInnerDto;
import ru.practicum.shareit.request.dto.ItemRequestOutLongDto;
import ru.practicum.shareit.request.dto.ItemRequestOutShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import java.util.List;

import static ru.practicum.shareit.consts.ShareItAppConst.HEADER_CALLER_ID;
import static ru.practicum.shareit.log.Logger.logRequest;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestOutShortDto addItemRequest(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                 @Valid @RequestBody ItemRequestInnerDto itemRequestInnerDto) {
        logRequest(HttpMethod.POST, "/requests",
                HEADER_CALLER_ID + userId, itemRequestInnerDto.toString());
        return itemRequestService.addItemRequest(userId, itemRequestInnerDto);
    }

    @GetMapping
    public List<ItemRequestOutLongDto> getYourItemRequests(@RequestHeader(HEADER_CALLER_ID) long userId) {
        logRequest(HttpMethod.GET, "/requests",
                HEADER_CALLER_ID + userId, "no");
        return itemRequestService.getYourItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestOutLongDto> getItemRequestsFromOthers(
            @RequestHeader(HEADER_CALLER_ID) long userId,
            @RequestParam(required = false) @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {
        logRequest(HttpMethod.GET, String.format("/requests/all?from=%s&size=%s", from, size),
                HEADER_CALLER_ID + userId, "no");
        return itemRequestService.getItemRequestsFromOthers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestOutLongDto getItemRequestById(@RequestHeader(HEADER_CALLER_ID) long userId,
                                                    @PathVariable long requestId) {
        logRequest(HttpMethod.GET, "/requests/" + requestId,
                HEADER_CALLER_ID + userId, "no");
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}
