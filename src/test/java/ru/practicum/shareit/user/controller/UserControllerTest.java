package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @SneakyThrows
    @Test
    void addUser_whenUserIdIsNegative_thenMethodArgumentNotValidExceptionThrow() {
        long userId = -1L;
        UserDto userDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        String exceptionMessage = "Id must not be negative";

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(userService, never()).addUser(userDto);
    }

    @SneakyThrows
    @Test
    void addUser_whenUserEmailIsInvalid_thenMethodArgumentNotValidExceptionThrow() {
        String email = "user email";
        UserDto userDto = new UserDto()
                .setName("User name")
                .setEmail(email);
        String exceptionMessage = "Invalid email format";

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(userService, never()).addUser(userDto);
    }

    @SneakyThrows
    @ParameterizedTest()
    @ValueSource(longs = {0L, 1L})
    void addUser_whenUserIdIsPositiveOrZero_thenConflictExceptionThrow(Long userId) {
        UserDto userDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        String exceptionMessage = "The user id should be generated automatically";
        when(userService.addUser(userDto)).thenThrow(new ConflictException(exceptionMessage));

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ConflictException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(userService, times(1)).addUser(userDto);
    }

    @SneakyThrows
    @Test
    void addUser_whenEmailOrNameIsInvalid_thenValidationExceptionThrow() {
        UserDto userDto = new UserDto();
        String exceptionMessage = "Email or name is invalid";
        when(userService.addUser(userDto)).thenThrow(new ValidationException(exceptionMessage));

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ValidationException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(userService, times(1)).addUser(userDto);
    }

    @SneakyThrows
    @Test
    void addUser_whenUserIsValid_thenSaveUser() {
        UserDto userInnerDto = new UserDto()
                .setName("User name")
                .setEmail("user@yandex.ru");
        UserDto userOutDto = new UserDto()
                .setId(1L)
                .setName("User name")
                .setEmail("user@yandex.ru");
        when(userService.addUser(userInnerDto)).thenReturn(userOutDto);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userInnerDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userOutDto.getName())))
                .andExpect(jsonPath("$.email", is(userOutDto.getEmail())));
    }

    @SneakyThrows
    @Test
    void updateUser_whenUserIdIsNegative_thenMethodArgumentNotValidExceptionThrow() {
        long userId = -1L;
        UserDto userDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        String exceptionMessage = "Id must not be negative";

        mockMvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(userService, never()).updateUser(userId, userDto);
    }

    @SneakyThrows
    @Test
    void updateUser_whenUserEmailIsInvalid_thenMethodArgumentNotValidExceptionThrow() {
        String email = "user email";
        long userId = 1L;
        UserDto userDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail(email);
        String exceptionMessage = "Invalid email format";

        mockMvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        getMessageForMethodArgumentNotValidException(result.getResolvedException())));
        verify(userService, never()).updateUser(userId, userDto);
    }

    @SneakyThrows
    @Test
    void updateUser_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        UserDto userDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        String exceptionMessage = String.format("User with id %s not found", userId);
        when(userService.updateUser(userId, userDto))
                .thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
        verify(userService, times(1)).updateUser(userId, userDto);
    }

    @SneakyThrows
    @Test
    void updateUser_whenUserIsValid_thenUpdateUser() {
        long userId = 1L;
        UserDto userDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        when(userService.updateUser(userId, userDto))
                .thenReturn(userDto);

        mockMvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @SneakyThrows
    @Test
    void getUserById_whenUserFound_thenReturnUserDto() {
        long userId = 1L;
        UserDto userDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        when(userService.getUserById(userId)).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @SneakyThrows
    @Test
    void getUserById_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = -1L;
        String exceptionMessage = String.format("User with id %s not found", userId);
        when(userService.getUserById(userId))
                .thenThrow(new NotFoundException(exceptionMessage));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof NotFoundException))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @SneakyThrows
    @Test
    void delUserById_whenUserFound_thenDeleteUser() {
        long userId = 1L;

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());
        verify(userService, times(1)).delUserById(userId);
    }

    @SneakyThrows
    @Test
    void getUsers_whenUsersExist_thenReturnUsers() {
        UserDto userDto = new UserDto()
                .setId(1L)
                .setName("User name")
                .setEmail("user@yandex.ru");
        List<UserDto> users = List.of(userDto);
        when(userService.getUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())));
    }

    @SneakyThrows
    @Test
    void getUsers_whenUsersNotExist_thenReturnEmptyList() {
        List<UserDto> users = new ArrayList<>();
        when(userService.getUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
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