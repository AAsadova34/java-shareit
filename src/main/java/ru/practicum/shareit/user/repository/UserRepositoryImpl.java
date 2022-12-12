package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.shareit.log.Logger.logStorageChanges;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long id;

    @Override
    public User addUser(User user) {
        if (!users.containsKey(user.getId())) {
            checkEmail(user.getId(), user.getEmail());
            generateId();
            user.setId(id);
            users.put(user.getId(), user);
            logStorageChanges("Add", user.toString());
            return user;
        } else {
            throw new ConflictException(String.format("User with id %s already exists", user.getId()));
        }
    }

    @Override
    public User updateEmailUser(User user) {
        long userId = user.getId();
        checkIfUserExists(userId);
        checkEmail(userId, user.getEmail());
        users.get(userId).setEmail(user.getEmail());
        User userStorage = users.get(userId);
        logStorageChanges("Update", userStorage.toString());
        return userStorage;
    }

    @Override
    public User updateNameUser(User user) {
        long userId = user.getId();
        checkIfUserExists(userId);
        users.get(userId).setName(user.getName());
        User userStorage = users.get(userId);
        logStorageChanges("Update", userStorage.toString());
        return userStorage;
    }

    @Override
    public User getUserById(long id) {
        checkIfUserExists(id);
        return users.get(id);
    }

    @Override
    public void delUserById(long id) {
        checkIfUserExists(id);
        users.remove(id);
        logStorageChanges("Delete", String.format("User with id %s", id));
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    private void generateId() {
        id++;
    }

    private void checkEmail(long userId, String email) {
        for (User user : users.values()) {
            if (user.getId() != userId && user.getEmail().equals(email)) {
                throw new ConflictException(String.format("User with email %s already exists", email));
            }
        }
    }

    private void checkIfUserExists(long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }
    }
}
