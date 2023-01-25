package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentInnerDto;
import ru.practicum.shareit.item.comment.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemInnerDto;
import ru.practicum.shareit.item.dto.ItemOutLongDto;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static java.lang.Integer.parseInt;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.consts.ShareItAppConst.HEADER_CALLER_ID;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @SneakyThrows
    @Test
    void addItem_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        ItemInnerDto itemInnerDto = new ItemInnerDto();
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemInnerDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).addItem(anyLong(), any(ItemInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addItem_whenNameOrEmailOrAvailableIsInvalid_thenValidationExceptionThrow() {
        long userId = 1L;
        ItemInnerDto itemInnerDto = new ItemInnerDto();
        String exceptionMessage = "Name or email or available is invalid";
        when(itemService.addItem(userId, itemInnerDto)).thenThrow(new ValidationException(exceptionMessage));

        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemInnerDto))
                        .header(HEADER_CALLER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ValidationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, times(1)).addItem(userId, itemInnerDto);
    }

    @SneakyThrows
    @Test
    void addItem_whenUserOrItemRequestNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        Long requestId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setRequestId(requestId);
        String exceptionMessage = String.format("User with id %s or itemRequest with id %s not found",
                userId, requestId);
        when(itemService.addItem(userId, itemInnerDto)).thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemInnerDto))
                        .header(HEADER_CALLER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, times(1)).addItem(userId, itemInnerDto);
    }

    @SneakyThrows
    @Test
    void addItem_whenItemIsValid_thenSaveItem() {
        long userId = 1L;
        Long requestId = 2L;
        Long itemId = 3L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestId);
        ItemOutShortDto itemOutDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestId);

        when(itemService.addItem(userId, itemInnerDto)).thenReturn(itemOutDto);

        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemInnerDto))
                        .header(HEADER_CALLER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$.description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(itemOutDto.getRequestId()), Long.class));
    }


    @SneakyThrows
    @Test
    void updateItem_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        Long itemId = 1L;
        ItemInnerDto itemInnerDto = new ItemInnerDto();
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(itemInnerDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).updateItem(anyLong(), anyLong(), any(ItemInnerDto.class));
    }

    @SneakyThrows
    @Test
    void updateItem_whenUserOrItemNotFoundOrUserIsNotOwner_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto();
        String exceptionMessage = "User or item not found or user is not owner";
        when(itemService.updateItem(userId, itemId, itemInnerDto)).thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(itemInnerDto))
                        .header(HEADER_CALLER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, times(1)).updateItem(userId, itemId, itemInnerDto);
    }

    @SneakyThrows
    @Test
    void updateItem_whenItemIsValid_thenUpdateItem() {
        long userId = 1L;
        Long itemId = 2L;
        ItemInnerDto itemInnerDto = new ItemInnerDto()
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        ItemOutShortDto itemOutDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);

        when(itemService.updateItem(userId, itemId, itemInnerDto)).thenReturn(itemOutDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(itemInnerDto))
                        .header(HEADER_CALLER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$.description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(itemOutDto.getRequestId()), Long.class));
    }

    @SneakyThrows
    @Test
    void getItemById_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        Long itemId = 1L;
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).getItemById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void getItemById_whenUserOrItemNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 1L;
        String exceptionMessage = String.format("User with id %s or item with id %s not found ",
                userId, itemId);
        when(itemService.getItemById(userId, itemId)).thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, times(1)).getItemById(userId, itemId);
    }

    @SneakyThrows
    @Test
    void getItemById_whenUserOrItemFound_thenReturnItem() {
        long userId = 1L;
        long itemId = 2L;
        ItemOutLongDto itemOutDto = new ItemOutLongDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setLastBooking(new BookingForItemDto())
                .setNextBooking(new BookingForItemDto())
                .setComments(List.of(new CommentOutDto()));
        when(itemService.getItemById(userId, itemId)).thenReturn(itemOutDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$.description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.lastBooking", is(itemOutDto.getLastBooking()), BookingForItemDto.class))
                .andExpect(jsonPath("$.nextBooking", is(itemOutDto.getNextBooking()), BookingForItemDto.class))
                .andExpect(jsonPath("$.comments", hasSize(1)));
    }

    @SneakyThrows
    @Test
    void getItems_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).getItems(anyLong(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getItems_whenFromIsNegative_thenConstraintViolationExceptionThrow() {
        long userId = 1L;
        String from = "-1";
        String size = "2";
        String exceptionMessage = "getItems.from: must be greater than or equal to 0";

        mockMvc.perform(get("/items", from, size)
                        .header(HEADER_CALLER_ID, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).getItems(anyLong(), anyInt(), anyInt());
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {"-1", "0"})
    void getItems_whenSizeIsNegative_thenConstraintViolationExceptionThrow(String size) {
        long userId = 1L;
        String from = "0";
        String exceptionMessage = "getItems.size: must be greater than 0";

        mockMvc.perform(get("/items", from, size)
                        .header(HEADER_CALLER_ID, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).getItems(anyLong(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getItems_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        String from = "0";
        String size = "1";
        String exceptionMessage = String.format("User with id %s not found", userId);
        when(itemService.getItems(userId, parseInt(from), parseInt(size)))
                .thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(get("/items")
                        .header(HEADER_CALLER_ID, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, times(1)).getItems(userId, parseInt(from), parseInt(size));
    }

    @SneakyThrows
    @Test
    void getItems_whenFromAndSizeIsNotNull_thenReturnLimitedList() {
        long userId = 1L;
        String from = "0";
        String size = "1";
        long itemId = 2L;
        ItemOutLongDto itemOutDto = new ItemOutLongDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setLastBooking(new BookingForItemDto())
                .setNextBooking(new BookingForItemDto())
                .setComments(List.of(new CommentOutDto()));
        List<ItemOutLongDto> items = List.of(itemOutDto);
        when(itemService.getItems(userId, parseInt(from), parseInt(size))).thenReturn(items);

        mockMvc.perform(get("/items", from, size)
                        .header(HEADER_CALLER_ID, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[0].lastBooking", is(itemOutDto.getLastBooking()), BookingForItemDto.class))
                .andExpect(jsonPath("$[0].nextBooking", is(itemOutDto.getNextBooking()), BookingForItemDto.class))
                .andExpect(jsonPath("$[0].comments", hasSize(1)));
    }

    @SneakyThrows
    @Test
    void getItems_whenFromAndSizeIsNull_thenReturnFullList() {
        long userId = 1L;
        long itemId = 2L;
        ItemOutLongDto itemOutDto = new ItemOutLongDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setLastBooking(new BookingForItemDto())
                .setNextBooking(new BookingForItemDto())
                .setComments(List.of(new CommentOutDto()));
        List<ItemOutLongDto> items = List.of(itemOutDto);
        when(itemService.getItems(userId, null, null)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[0].lastBooking", is(itemOutDto.getLastBooking()), BookingForItemDto.class))
                .andExpect(jsonPath("$[0].nextBooking", is(itemOutDto.getNextBooking()), BookingForItemDto.class))
                .andExpect(jsonPath("$[0].comments", hasSize(1)));
    }

    @SneakyThrows
    @Test
    void findItemsByNameOrDescription_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        String text = "Search text";
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).findItemsByNameOrDescription(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void findItemsByNameOrDescription_whenFromIsNegative_thenConstraintViolationExceptionThrow() {
        long userId = 1L;
        String text = "Search text";
        String from = "-1";
        String size = "2";
        String exceptionMessage = "findItemsByNameOrDescription.from: must be greater than or equal to 0";

        mockMvc.perform(get("/items/search")
                        .header(HEADER_CALLER_ID, userId)
                        .param("text", text)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).findItemsByNameOrDescription(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {"-1", "0"})
    void findItemsByNameOrDescription_whenSizeIsNegative_thenConstraintViolationExceptionThrow(String size) {
        long userId = 1L;
        String text = "Search text";
        String from = "0";
        String exceptionMessage = "findItemsByNameOrDescription.size: must be greater than 0";

        mockMvc.perform(get("/items/search")
                        .header(HEADER_CALLER_ID, userId)
                        .param("text", text)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).findItemsByNameOrDescription(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void findItemsByNameOrDescription_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        String text = "Search text";
        String from = "0";
        String size = "1";
        String exceptionMessage = String.format("User with id %s not found", userId);
        when(itemService.findItemsByNameOrDescription(userId, text, parseInt(from), parseInt(size)))
                .thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(get("/items/search")
                        .header(HEADER_CALLER_ID, userId)
                        .param("text", text)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, times(1))
                .findItemsByNameOrDescription(userId, text, parseInt(from), parseInt(size));

    }

    @SneakyThrows
    @Test
    void findItemsByNameOrDescription_whenFromAndSizeIsNotNull_thenReturnLimitedList() {
        long userId = 1L;
        String text = "item";
        String from = "0";
        String size = "1";
        long itemId = 2L;
        long requestId = 3L;
        ItemOutShortDto itemOutDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestId);
        List<ItemOutShortDto> items = List.of(itemOutDto);
        when(itemService.findItemsByNameOrDescription(userId, text, parseInt(from), parseInt(size)))
                .thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .header(HEADER_CALLER_ID, userId)
                        .param("text", text)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[0].requestId", is(itemOutDto.getRequestId()), Long.class));
    }

    @SneakyThrows
    @Test
    void findItemsByNameOrDescription_whenFromAndSizeIsNull_thenReturnFullList() {
        long userId = 1L;
        String text = "item";
        long itemId = 2L;
        long requestId = 3L;
        ItemOutShortDto itemOutDto = new ItemOutShortDto()
                .setId(itemId)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestId);
        List<ItemOutShortDto> items = List.of(itemOutDto);
        when(itemService.findItemsByNameOrDescription(userId, text, null, null))
                .thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .header(HEADER_CALLER_ID, userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[0].requestId", is(itemOutDto.getRequestId()), Long.class));
    }

    @SneakyThrows
    @Test
    void addComment_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        long itemId = 1L;
        CommentInnerDto comment = new CommentInnerDto("Item is great");
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, never()).addComment(anyLong(), anyLong(), any(CommentInnerDto.class));
    }

    @SneakyThrows
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addComment_whenCommentTextIsInvalid_thenMethodArgumentNotValidExceptionThrow(String text) {
        long userId = 1L;
        long itemId = 2L;
        CommentInnerDto comment = new CommentInnerDto(text);
        String exceptionMessage = "Text comment text must not be empty";

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HEADER_CALLER_ID, userId)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(itemService, never()).addComment(anyLong(), anyLong(), any(CommentInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addComment_whenUserOrItemNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long itemId = 2L;
        CommentInnerDto comment = new CommentInnerDto("Item is great");
        String exceptionMessage = String.format("User with id %s or item with id %s not found", userId, itemId);
        when(itemService.addComment(userId, itemId, comment))
                .thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HEADER_CALLER_ID, userId)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, times(1)).addComment(userId, itemId, comment);
    }

    @SneakyThrows
    @Test
    void addComment_whenUserHasNotBooker_thenValidationExceptionThrow() {
        long userId = 1L;
        long itemId = 2L;
        CommentInnerDto comment = new CommentInnerDto("Item is great");
        String exceptionMessage = String.format("User with id %s did not book the item with id %s " +
                "or the reservation has not ended yet.", userId, itemId);
        when(itemService.addComment(userId, itemId, comment))
                .thenThrow(new ValidationException(exceptionMessage));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HEADER_CALLER_ID, userId)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ValidationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(itemService, times(1)).addComment(userId, itemId, comment);
    }

    @SneakyThrows
    @Test
    void addComment_whenCommentIsValid_thenSaveComment() {
        long userId = 1L;
        long itemId = 2L;
        long commentId = 3L;
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        CommentInnerDto commentInner = new CommentInnerDto("Item is great");
        CommentOutDto commentOut = new CommentOutDto()
                .setId(commentId)
                .setText("Item is great")
                .setAuthorName("User name")
                .setCreated(created);
        when(itemService.addComment(userId, itemId, commentInner))
                .thenReturn(commentOut);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HEADER_CALLER_ID, userId)
                        .content(mapper.writeValueAsString(commentInner))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentOut.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentOut.getText())))
                .andExpect(jsonPath("$.authorName", is(commentOut.getAuthorName())))
                .andExpect(jsonPath("$.created", is(created.toString())));
    }

    private String getMessageForMethodArgumentNotValidException(Exception e) {
        String message = "";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException eValidation = (MethodArgumentNotValidException) e;
            message = Objects.requireNonNull(eValidation.getBindingResult().getFieldError()).getDefaultMessage();
        }
        return message;
    }
}