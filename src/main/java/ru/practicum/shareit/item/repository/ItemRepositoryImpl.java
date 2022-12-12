package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.log.Logger.logStorageChanges;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long id;

    @Override
    public Item addItem(Item item) {
        if (!items.containsKey(item.getId())) {
            generateId();
            item.setId(id);
            items.put(item.getId(), item);
            logStorageChanges("Add", item.toString());
            return item;
        } else {
            throw new ConflictException(String.format("Item with id %s already exists", item.getId()));
        }
    }

    @Override
    public Item updateNameItem(Item item) {
        long itemId = item.getId();
        checkIfItemExists(itemId);
        checkOwner(item.getUserId(), itemId);
        items.get(itemId).setName(item.getName());
        Item itemStorage = items.get(itemId);
        logStorageChanges("Update", itemStorage.toString());
        return itemStorage;
    }

    @Override
    public Item updateDescriptionItem(Item item) {
        long itemId = item.getId();
        checkIfItemExists(itemId);
        checkOwner(item.getUserId(), itemId);
        items.get(itemId).setDescription(item.getDescription());
        Item itemStorage = items.get(itemId);
        logStorageChanges("Update", itemStorage.toString());
        return itemStorage;
    }

    @Override
    public Item updateAvailableItem(Item item) {
        long itemId = item.getId();
        checkIfItemExists(itemId);
        checkOwner(item.getUserId(), itemId);
        items.get(itemId).setAvailable(item.getAvailable());
        Item itemStorage = items.get(itemId);
        logStorageChanges("Update", itemStorage.toString());
        return itemStorage;
    }

    @Override
    public Item getItemById(long itemId) {
        checkIfItemExists(itemId);
        return items.get(itemId);
    }

    @Override
    public List<Item> getItems(long userId) {
        return items.values().stream()
                .filter(item -> item.getUserId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findByNameOrDescription(String text) {
        return items.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                        && item.getAvailable())
                .collect(Collectors.toList());
    }

    private void generateId() {
        id++;
    }

    private void checkOwner(long userId, long itemId) {
        long ownerId = items.get(itemId).getUserId();
        if (userId != ownerId) {
            throw new NotFoundException(String.format("The user with id %s cannot change the user with id %s item",
                    userId, ownerId));
        }
    }

    private void checkIfItemExists(long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException(String.format("Item with id %s not found", itemId));
        }
    }
}
