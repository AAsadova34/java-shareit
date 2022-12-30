package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.dto.ItemMapper.convertToItem;
import static ru.practicum.shareit.item.dto.ItemMapper.convertToItemDto;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        userService.getUserById(userId);
        Item item = convertToItem(userId, itemDto.getId(), itemDto);
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Name must not be null or empty");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Description must not be null or empty");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Available must not be null");
        }
        Item itemStorage = itemRepository.addItem(item);
        return convertToItemDto(itemStorage.getId(), itemStorage);
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        userService.getUserById(userId);
        Item item = convertToItem(userId, itemId, itemDto);
        Item itemStorage = Item.builder().build();
        if (item.getName() != null && !item.getName().isBlank()) {
            itemStorage = itemRepository.updateNameItem(item);
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            itemStorage = itemRepository.updateDescriptionItem(item);
        }
        if (item.getAvailable() != null) {
            itemStorage = itemRepository.updateAvailableItem(item);
        }
        return convertToItemDto(itemStorage.getId(), itemStorage);
    }

    @Override
    public ItemDto getItemById(long userId, long itemId) {
        userService.getUserById(userId);
        Item itemStorage = itemRepository.getItemById(itemId);
        return convertToItemDto(itemStorage.getId(), itemStorage);
    }

    @Override
    public List<ItemDto> getItems(long userId) {
        userService.getUserById(userId);
        return itemRepository.getItems(userId).stream()
                .map(item -> convertToItemDto(item.getId(), item))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findByNameOrDescription(long userId, String text) {
        userService.getUserById(userId);
        if (text != null && !text.isBlank()) {
            return itemRepository.findByNameOrDescription(text).stream()
                    .map(item -> convertToItemDto(item.getId(), item))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
