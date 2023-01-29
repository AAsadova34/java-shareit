package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.log.Logger.logStorageChanges;
import static ru.practicum.shareit.user.mapper.UserMapper.*;
import static ru.practicum.shareit.validation.Validation.checkUserExists;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDto.getId() != null) {
            throw new ConflictException("The user id should be generated automatically");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email must not be null or empty");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Name must not be null or empty");
        }
        User user = toUser(userDto.getId(), userDto);
        User userStorage = userRepository.save(user);
        logStorageChanges("Add", userStorage.toString());
        return toUserDto(userStorage.getId(), userStorage);
    }

    @Transactional
    @Override
    public UserDto updateUser(long id, UserDto userDto) {
        checkUserExists(userRepository, id);
        User oldUser = userRepository.getReferenceById(id);
        User newUser = toUser(id, userDto);
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        }
        User userStorage = userRepository.save(oldUser);
        logStorageChanges("Update", userStorage.toString());
        return toUserDto(userStorage.getId(), userStorage);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(long id) {
        checkUserExists(userRepository, id);
        User userStorage = userRepository.getReferenceById(id);
        return toUserDto(userStorage.getId(), userStorage);
    }

    @Transactional
    @Override
    public void delUserById(long id) {
        checkUserExists(userRepository, id);
        userRepository.deleteById(id);
        logStorageChanges("Delete", String.format("User with id %s", id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> toUserDto(user.getId(), user))
                .collect(Collectors.toList());
    }
}
