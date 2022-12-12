package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UserControllerTest {
    UserController userController;

    @BeforeEach
    void initialize() {
        userController = new UserController(new UserServiceImpl(new UserRepositoryImpl()));
    }

    @Test
    void addUserTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        userDto.setId(1);
        assertThat(userDto, equalTo(newUser));
    }

    @Test
    void addUserWithDuplicateIdTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto1);
        UserDto userDtoDuplicateId = UserDto.builder()
                .id(newUser.getId())
                .email("user2@Ya.ru")
                .name("User2")
                .build();
        ConflictException e = Assertions.assertThrows(
                ConflictException.class, () -> userController.addUser(userDtoDuplicateId));
        assertThat("User with id 1 already exists", equalTo(e.getMessage()));
    }

    @Test
    void addUserWithDuplicateEmailTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto userDto2 = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        userController.addUser(userDto1);
        ConflictException e = Assertions.assertThrows(
                ConflictException.class, () -> userController.addUser(userDto2));
        assertThat("User with email user@Ya.ru already exists", equalTo(e.getMessage()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addUserWithNullOrEmptyEmailTest(String email) {
        UserDto userDto = UserDto.builder()
                .email(email)
                .name("User")
                .build();
        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> userController.addUser(userDto));
        assertThat("Email must not be null or empty", equalTo(e.getMessage()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addUserWithNullOrEmptyNameTest(String name) {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name(name)
                .build();
        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> userController.addUser(userDto));
        assertThat("Name must not be null or empty", equalTo(e.getMessage()));
    }

    @Test
    void updateUserTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto newUser = userController.addUser(userDto1);
        UserDto userDto2 = UserDto.builder()
                .id(newUser.getId())
                .email("newUser1@Ya.ru")
                .name("newUser1")
                .build();
        UserDto updateUser = userController.updateUser(newUser.getId(), userDto2);
        assertThat(userDto2, equalTo(updateUser));
    }

    @Test
    void updateUserEmailTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto newUser = userController.addUser(userDto1);
        UserDto userDto2 = UserDto.builder()
                .id(newUser.getId())
                .email("newUser1@Ya.ru")
                .name("User1")
                .build();
        UserDto updateUser = userController.updateUser(newUser.getId(), userDto2);
        assertThat(userDto2, equalTo(updateUser));
    }

    @Test
    void updateUserNameTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto newUser = userController.addUser(userDto1);
        UserDto userDto2 = UserDto.builder()
                .id(newUser.getId())
                .email("user1@Ya.ru")
                .name("newUser1")
                .build();
        UserDto updateUser = userController.updateUser(newUser.getId(), userDto2);
        assertThat(userDto2, equalTo(updateUser));
    }

    @Test
    void updateUserWithDuplicateEmailTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto userDto2 = UserDto.builder()
                .email("user2@Ya.ru")
                .name("User2")
                .build();
        UserDto newUser1 = userController.addUser(userDto1);
        UserDto newUser2 = userController.addUser(userDto2);
        UserDto userDto3 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User")
                .build();

        ConflictException e = Assertions.assertThrows(
                ConflictException.class, () -> userController.updateUser(newUser2.getId(), userDto3));
        assertThat("User with email user1@Ya.ru already exists", equalTo(e.getMessage()));
    }

    @Test
    void updateUserWithFailIdTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> userController.updateUser(-1, userDto));
        assertThat("User with id -1 not found", equalTo(e.getMessage()));
    }

    @Test
    void getUserByIdTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto newUser = userController.addUser(userDto1);
        assertThat(newUser, equalTo(userController.getUserById(newUser.getId())));
    }

    @Test
    void getUserByFailIdTest() {
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> userController.getUserById(-1));
        assertThat("User with id -1 not found", equalTo(e.getMessage()));
    }

    @Test
    void delUserById() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto userDto2 = UserDto.builder()
                .email("user2@Ya.ru")
                .name("User2")
                .build();
        UserDto newUser1 = userController.addUser(userDto1);
        UserDto newUser2 = userController.addUser(userDto2);
        userController.delUserById(newUser1.getId());
        assertThat(userController.getUsers(), hasItem(newUser2));
        assertThat(userController.getUsers(), not(hasItem(newUser1)));
    }

    @Test
    void delUserByFailId() {
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> userController.delUserById(-1));
        assertThat("User with id -1 not found", equalTo(e.getMessage()));
    }

    @Test
    void getUsersTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto userDto2 = UserDto.builder()
                .email("user2@Ya.ru")
                .name("User2")
                .build();
        UserDto newUser1 = userController.addUser(userDto1);
        UserDto newUser2 = userController.addUser(userDto2);
        assertThat(userController.getUsers(), hasItem(newUser1));
        assertThat(userController.getUsers(), hasItem(newUser2));
        assertThat(userController.getUsers(), hasSize(2));
    }

    @Test
    void getUsersEmptyTest() {
        assertThat(userController.getUsers(), empty());
    }
}
