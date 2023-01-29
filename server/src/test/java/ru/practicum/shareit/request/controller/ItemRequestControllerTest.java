package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.request.dto.ItemRequestInnerDto;
import ru.practicum.shareit.request.dto.ItemRequestOutLongDto;
import ru.practicum.shareit.request.dto.ItemRequestOutShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static java.lang.Integer.parseInt;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.consts.ShareItAppConst.HEADER_CALLER_ID;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService requestService;

    @SneakyThrows
    @Test
    void addItemRequest_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        ItemRequestInnerDto requestInner = new ItemRequestInnerDto("Some item");
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestInner))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(requestService, never()).addItemRequest(anyLong(), any(ItemRequestInnerDto.class));
    }

    @SneakyThrows
    @Test
    void addItemRequest_whenUserIsNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        ItemRequestInnerDto requestInner = new ItemRequestInnerDto("Some item");
        String exceptionMessage = String.format("User with id %s not found", userId);
        when(requestService.addItemRequest(userId, requestInner)).thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestInner))
                        .header(HEADER_CALLER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(requestService, times(1)).addItemRequest(userId, requestInner);
    }

    @SneakyThrows
    @Test
    void addItemRequest_ItemRequestIsValid_thenSaveItemRequest() {
        long userId = 1L;
        long requestId = 2L;
        ItemRequestInnerDto requestInner = new ItemRequestInnerDto("Some item");
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ItemRequestOutShortDto requestOut = new ItemRequestOutShortDto()
                .setId(requestId)
                .setDescription("Some item")
                .setCreated(created);
        when(requestService.addItemRequest(userId, requestInner)).thenReturn(requestOut);

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestInner))
                        .header(HEADER_CALLER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestId), Long.class))
                .andExpect(jsonPath("$.description", is(requestOut.getDescription())))
                .andExpect(jsonPath("$.created", is(created.toString())));
    }

    @SneakyThrows
    @Test
    void getYourItemRequests_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(requestService, never()).getYourItemRequests(anyLong());

    }

    @SneakyThrows
    @Test
    void getYourItemRequests_whenUserIsNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        String exceptionMessage = String.format("User with id %s not found", userId);
        when(requestService.getYourItemRequests(userId)).thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(get("/requests")
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(requestService, times(1)).getYourItemRequests(userId);
    }

    @SneakyThrows
    @Test
    void getYourItemRequests_whenYourItemRequestsIsFound_ReturnItemRequests() {
        long userId = 1L;
        long requestId = 2L;
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ItemRequestOutLongDto requestOut = new ItemRequestOutLongDto()
                .setId(requestId)
                .setDescription("Some item")
                .setCreated(created)
                .setItems(List.of(new ItemOutShortDto()));
        List<ItemRequestOutLongDto> requests = List.of(requestOut);
        when(requestService.getYourItemRequests(userId)).thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestId), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestOut.getDescription())))
                .andExpect(jsonPath("$[0].created", is(created.toString())))
                .andExpect(jsonPath("$[0].items", hasSize(1)));
    }

    @SneakyThrows
    @Test
    void getItemRequestsFromOthers_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(requestService, never()).getItemRequestsFromOthers(anyLong(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getItemRequestsFromOthers_whenUserIsNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        String from = "0";
        String size = "1";
        String exceptionMessage = String.format("User with id %s not found", userId);
        when(requestService.getItemRequestsFromOthers(userId, parseInt(from), parseInt(size)))
                .thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(get("/requests/all")
                        .header(HEADER_CALLER_ID, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(requestService, times(1))
                .getItemRequestsFromOthers(userId, parseInt(from), parseInt(size));
    }

    @SneakyThrows
    @Test
    void getItemRequestsFromOthers_whenFromAndSizeIsNotNull_thenReturnLimitedList() {
        long userId = 1L;
        String from = "0";
        String size = "1";
        long requestId = 2L;
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ItemRequestOutLongDto requestOut = new ItemRequestOutLongDto()
                .setId(requestId)
                .setDescription("Some item")
                .setCreated(created)
                .setItems(List.of(new ItemOutShortDto()));
        List<ItemRequestOutLongDto> requests = List.of(requestOut);
        when(requestService.getItemRequestsFromOthers(userId, parseInt(from), parseInt(size)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header(HEADER_CALLER_ID, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestId), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestOut.getDescription())))
                .andExpect(jsonPath("$[0].created", is(created.toString())))
                .andExpect(jsonPath("$[0].items", hasSize(1)));
    }

    @SneakyThrows
    @Test
    void getItemRequestsFromOthers_whenFromAndSizeIsNull_thenReturnFullList() {
        long userId = 1L;
        long requestId = 2L;
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ItemRequestOutLongDto requestOut = new ItemRequestOutLongDto()
                .setId(requestId)
                .setDescription("Some item")
                .setCreated(created)
                .setItems(List.of(new ItemOutShortDto()));
        List<ItemRequestOutLongDto> requests = List.of(requestOut);
        when(requestService.getItemRequestsFromOthers(userId, null, null)).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestId), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestOut.getDescription())))
                .andExpect(jsonPath("$[0].created", is(created.toString())))
                .andExpect(jsonPath("$[0].items", hasSize(1)));
    }

    @SneakyThrows
    @Test
    void getItemRequestById_whenNotRequestHeader_thenMissingRequestHeaderExceptionThrow() {
        long requestId = 1L;
        String exceptionMessage = String.format("Required request header '%s' " +
                "for method parameter type long is not present", HEADER_CALLER_ID);

        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingRequestHeaderException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(requestService, never()).getItemRequestById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void getItemRequestById_whenUserOrRequestIsNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        long requestId = 1L;
        String exceptionMessage = String.format("User with id %s or request with id %s is not found", userId, requestId);
        when(requestService.getItemRequestById(userId, requestId))
                .thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(requestService, times(1)).getItemRequestById(userId, requestId);
    }

    @SneakyThrows
    @Test
    void getItemRequestById_whenRequestIsValid_thenReturnItemRequestById() {
        long userId = 1L;
        long requestId = 1L;
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ItemRequestOutLongDto requestOut = new ItemRequestOutLongDto()
                .setId(requestId)
                .setDescription("Some item")
                .setCreated(created)
                .setItems(List.of(new ItemOutShortDto()));

        when(requestService.getItemRequestById(userId, requestId)).thenReturn(requestOut);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(HEADER_CALLER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestId), Long.class))
                .andExpect(jsonPath("$.description", is(requestOut.getDescription())))
                .andExpect(jsonPath("$.created", is(created.toString())))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }
}