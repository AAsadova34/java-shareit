package ru.practicum.shareit.user.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    @Transactional
    UserDto addUser(UserDto userDto);

    @Transactional
    UserDto updateUser(long id, UserDto userDto);

    @Transactional(readOnly = true)
    UserDto getUserById(long id);

    @Transactional
    void delUserById(long id);

    @Transactional(readOnly = true)
    List<UserDto> getUsers();

}
