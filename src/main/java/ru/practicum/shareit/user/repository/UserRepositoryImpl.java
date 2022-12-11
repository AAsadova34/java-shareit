package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.log.Logger;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> userMap = new HashMap<>();
    private long id;

    @Override
    public User addUser(User user) {
        if (!userMap.containsKey(user.getId())) {
            checkEmail(user.getEmail());
            generateId();
            user.setId(id);
            userMap.put(user.getId(), user);
            Logger.logStorageChanges("Add", user.toString());
            return user;
        } else {
            throw new ConflictException(String.format("User with id %s already exists", user.getId()));
        }
    }

    @Override
    public User updateEmailUser(User user) {
        long userId = user.getId();
        checkIfUserExists(userId);
        checkEmail(user.getEmail());
        userMap.get(userId).setEmail(user.getEmail());
        User userStorage = userMap.get(userId);
        Logger.logStorageChanges("Update", userStorage.toString());
        return userStorage;
    }

    @Override
    public User updateNameUser(User user) {
        long userId = user.getId();
        checkIfUserExists(userId);
        userMap.get(userId).setName(user.getName());
        User userStorage = userMap.get(userId);
        Logger.logStorageChanges("Update", userStorage.toString());
        return userStorage;
    }

    @Override
    public User getUserById(long id) {
        checkIfUserExists(id);
        return userMap.get(id);
    }

    @Override
    public void delUserById(long id) {
        checkIfUserExists(id);
        userMap.remove(id);
        Logger.logStorageChanges("Delete", String.format("User with id %s", id));
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(userMap.values());
    }

    private void generateId() {
        id++;
    }

    private void checkEmail(String email) {
        for (User user : userMap.values()) {
            if (user.getEmail().equals(email)) {
                throw new ConflictException(String.format("User with email %s already exists",
                        email));
            }
        }
    }

    private void checkIfUserExists(long userId) {
        if (!userMap.containsKey(userId)) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }
    }
}
