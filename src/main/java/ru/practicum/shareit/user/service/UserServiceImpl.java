package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.dto.UserMapper.convertToUser;
import static ru.practicum.shareit.user.dto.UserMapper.convertToUserDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = convertToUser(userDto.getId(), userDto);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email must not be null or empty");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("Name must not be null or empty");
        }
        User userStorage = userRepository.addUser(user);
        return convertToUserDto(userStorage.getId(), userStorage);
    }

    @Override
    public UserDto updateUser(long id, UserDto userDto) {
        User user = convertToUser(id, userDto);
        User userStorage = User.builder().build();
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            userStorage = userRepository.updateEmailUser(user);
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            userStorage = userRepository.updateNameUser(user);
        }
        return convertToUserDto(userStorage.getId(), userStorage);
    }

    @Override
    public UserDto getUserById(long id) {
        User userStorage = userRepository.getUserById(id);
        return convertToUserDto(userStorage.getId(), userStorage);
    }

    @Override
    public void delUserById(long id) {
        userRepository.delUserById(id);
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.getUsers().stream()
                .map(user -> convertToUserDto(user.getId(), user))
                .collect(Collectors.toList());
    }
}
