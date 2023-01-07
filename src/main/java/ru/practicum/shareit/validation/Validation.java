package ru.practicum.shareit.validation;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

@UtilityClass
public class Validation {
    public static void checkUserExists(UserRepository userRepository, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }
    }

    public static void checkItemExists(ItemRepository itemRepository, long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException(String.format("Item with id %s not found", itemId));
        }
    }
}
