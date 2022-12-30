package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item addItem(Item item);

    Item updateNameItem(Item item);

    Item updateDescriptionItem(Item item);

    Item updateAvailableItem(Item item);

    Item getItemById(long itemId);

    List<Item> getItems(long userId);

    List<Item> findByNameOrDescription(String text);
}
