package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void addUser_whenUserDtoIdIsNotNull_thenConflictExceptionThrow() {
        long userId = 1L;
        UserDto userInnerDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");

        ConflictException e = Assertions.assertThrows(
                ConflictException.class, () -> userService.addUser(userInnerDto));
        assertThat("The user id should be generated automatically",
                equalTo(e.getMessage()));
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addUser_whenUserDtoEmailIsNullOrEmptyOrBlank_thenValidationExceptionThrow(String email) {
        UserDto userInnerDto = new UserDto()
                .setName("User name")
                .setEmail(email);

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> userService.addUser(userInnerDto));
        assertThat("Email must not be null or empty",
                equalTo(e.getMessage()));
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addUser_whenUserDtoNameIsNullOrEmptyOrBlank_thenValidationExceptionThrow(String name) {
        UserDto userInnerDto = new UserDto()
                .setName(name)
                .setEmail("user@yandex.ru");

        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> userService.addUser(userInnerDto));
        assertThat("Name must not be null or empty",
                equalTo(e.getMessage()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_whenUserDtoIsValid_thenSaveUser() {
        long userId = 1L;
        UserDto userInnerDto = new UserDto()
                .setName("User name")
                .setEmail("user@yandex.ru");
        User userStorage = new User()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        UserDto userOutDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        when(userRepository.save(any(User.class))).thenReturn(userStorage);

        UserDto actualUserDto = userService.addUser(userInnerDto);

        assertThat(userOutDto, equalTo(actualUserDto));
    }

    @Test
    void updateUser_whenUserNotFound_thenNotFoundExceptionThrow() {
        long userId = 1L;
        UserDto userInnerDto = new UserDto()
                .setName("User name")
                .setEmail("user@yandex.ru");
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> userService.updateUser(userId, userInnerDto));
        assertThat(String.format("User with id %s not found", userId),
                equalTo(e.getMessage()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_whenFilledInName_thenUpdateName() {
        long userId = 1L;
        UserDto userInnerDto = new UserDto()
                .setName("newUser name");
        User userStorage = new User()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        UserDto userOutDto = new UserDto()
                .setId(userId)
                .setName("newUser name")
                .setEmail("user@yandex.ru");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(userStorage);
        when(userRepository.save(userStorage)).thenReturn(userStorage);

        UserDto actualUserDto = userService.updateUser(userId, userInnerDto);

        assertThat(userOutDto, equalTo(actualUserDto));
    }

    @Test
    void updateUser_whenFilledInEmail_thenUpdateEmail() {
        long userId = 1L;
        UserDto userInnerDto = new UserDto()
                .setId(null)
                .setEmail("newUser@yandex.ru");
        User userStorage = new User()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        UserDto userOutDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("newUser@yandex.ru");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(userStorage);
        when(userRepository.save(userStorage)).thenReturn(userStorage);

        UserDto actualUserDto = userService.updateUser(userId, userInnerDto);

        assertThat(userOutDto, equalTo(actualUserDto));
    }

    @Test
    void updateUser_whenFilledInNameAndEmail_thenUpdateNameAndEmail() {
        long userId = 1L;
        UserDto userInnerDto = new UserDto()
                .setId(null)
                .setName("newUser name")
                .setEmail("newUser@yandex.ru");
        User userStorage = new User()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        UserDto userOutDto = new UserDto()
                .setId(userId)
                .setName("newUser name")
                .setEmail("newUser@yandex.ru");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(userStorage);
        when(userRepository.save(userStorage)).thenReturn(userStorage);

        UserDto actualUserDto = userService.updateUser(userId, userInnerDto);

        assertThat(userOutDto, equalTo(actualUserDto));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void updateUser_whenNewUserNameIsNullOrEmptyOrBlank_thenUpdateEmail(String name) {
        long userId = 1L;
        UserDto userInnerDto = new UserDto()
                .setId(null)
                .setName(name)
                .setEmail("newUser@yandex.ru");
        User userStorage = new User()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        UserDto userOutDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("newUser@yandex.ru");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(userStorage);
        when(userRepository.save(userStorage)).thenReturn(userStorage);

        UserDto actualUserDto = userService.updateUser(userId, userInnerDto);

        assertThat(userOutDto, equalTo(actualUserDto));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void updateUser_whenNewUserEmailIsNullOrEmptyOrBlank_thenUpdateName(String email) {
        long userId = 1L;
        UserDto userInnerDto = new UserDto()
                .setId(null)
                .setName("newUser name")
                .setEmail(email);
        User userStorage = new User()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        UserDto userOutDto = new UserDto()
                .setId(userId)
                .setName("newUser name")
                .setEmail("user@yandex.ru");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(userStorage);
        when(userRepository.save(userStorage)).thenReturn(userStorage);

        UserDto actualUserDto = userService.updateUser(userId, userInnerDto);

        assertThat(userOutDto, equalTo(actualUserDto));
    }

    @Test
    void getUserById_whenUserFound_thenReturnedUserDto() {
        long userId = 1L;
        User user = new User()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        UserDto userOutDto = new UserDto()
                .setId(userId)
                .setName("User name")
                .setEmail("user@yandex.ru");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        UserDto actualUserDto = userService.getUserById(userId);

        assertThat(userOutDto, equalTo(actualUserDto));
    }

    @Test
    void getUserById_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> userService.getUserById(userId));
        assertThat(String.format("User with id %s not found", userId),
                equalTo(e.getMessage()));
        verify(userRepository, never()).getReferenceById(userId);
    }

    @Test
    void delUserById_whenUserFound_thenUserIsDeleted() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        userService.delUserById(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void getUsers_whenThereAreNoUsers_thenReturnedEmptyListUserDto() {
        List<User> usersStorage = new ArrayList<>();
        when(userRepository.findAll()).thenReturn(usersStorage);

        List<UserDto> actualUsersDto = userService.getUsers();

        assertTrue(actualUsersDto.isEmpty());
    }

    @Test
    void getUsers_whenThereAreUsers_thenReturnedUserDtoList() {
        User user1 = new User()
                .setId(1L)
                .setName("User1 name")
                .setEmail("user1@yandex.ru");
        User user2 = new User()
                .setId(2L)
                .setName("User1 name")
                .setEmail("user1@yandex.ru");
        List<User> usersStorage = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(usersStorage);

        List<UserDto> actualUsersDto = userService.getUsers();

        assertThat(actualUsersDto, hasSize(2));
        assertThat(UserDto.class, equalTo(actualUsersDto.get(0).getClass()));
    }
}