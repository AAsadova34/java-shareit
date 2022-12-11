package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        userService.getUserById(userId);
        Item item = ItemMapper.convertToItem(userId, itemDto.getId(), itemDto);
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
        return ItemMapper.convertToItemDto(itemStorage.getId(), itemStorage);
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        userService.getUserById(userId);
        Item item = ItemMapper.convertToItem(userId, itemId, itemDto);
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
        return ItemMapper.convertToItemDto(itemStorage.getId(), itemStorage);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        Item itemStorage = itemRepository.getItemById(itemId);
        return ItemMapper.convertToItemDto(itemStorage.getId(), itemStorage);
    }

    @Override
    public List<ItemDto> getItems(long userId) {
        return itemRepository.getItems(userId).stream()
                .map(item -> ItemMapper.convertToItemDto(item.getId(), item))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findByNameOrDescription(String text) {
        if (text != null && !text.isBlank()) {
            return itemRepository.findByNameOrDescription(text).stream()
                    .map(item -> ItemMapper.convertToItemDto(item.getId(), item))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
