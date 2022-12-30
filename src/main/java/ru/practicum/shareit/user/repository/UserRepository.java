package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User addUser(User user);

    User updateEmailUser(User user);

    User updateNameUser(User user);

    User getUserById(long id);

    void delUserById(long id);

    List<User> getUsers();
}
