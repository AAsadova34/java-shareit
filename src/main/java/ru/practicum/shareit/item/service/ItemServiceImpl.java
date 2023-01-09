package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentInnerDto;
import ru.practicum.shareit.item.comment.dto.CommentOutDto;
import ru.practicum.shareit.item.comment.mapper.CommentsMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemInnerDto;
import ru.practicum.shareit.item.dto.ItemOutLongDto;
import ru.practicum.shareit.item.dto.ItemOutShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingForItemDto;
import static ru.practicum.shareit.item.comment.mapper.CommentsMapper.toComment;
import static ru.practicum.shareit.item.comment.mapper.CommentsMapper.toCommentOutDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;
import static ru.practicum.shareit.log.Logger.logStorageChanges;
import static ru.practicum.shareit.validation.Validation.checkItemExists;
import static ru.practicum.shareit.validation.Validation.checkUserExists;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemOutShortDto addItem(long userId, ItemInnerDto itemInnerDto) {
        checkUserExists(userRepository, userId);
        if (itemInnerDto.getName() == null || itemInnerDto.getName().isBlank()) {
            throw new ValidationException("Name must not be null or empty");
        }
        if (itemInnerDto.getDescription() == null || itemInnerDto.getDescription().isBlank()) {
            throw new ValidationException("Description must not be null or empty");
        }
        if (itemInnerDto.getAvailable() == null) {
            throw new ValidationException("Available must not be null");
        }
        Item item = toItem(userId, itemInnerDto);
        Item itemStorage = itemRepository.save(item);
        logStorageChanges("Add item", itemStorage.toString());
        return toItemOutShortDto(itemStorage.getId(), itemStorage);
    }

    @Transactional
    @Override
    public ItemOutShortDto updateItem(long userId, long itemId, ItemInnerDto itemInnerDto) {
        checkUserExists(userRepository, userId);
        checkItemExists(itemRepository, itemId);
        Item oldItem = itemRepository.getReferenceById(itemId);
        if (userId != oldItem.getUserId()) {
            throw new NotFoundException(String.format("The user with id %s cannot change an item that he does not own",
                    userId));
        }
        Item newItem = toItem(userId, itemInnerDto);
        if (newItem.getName() != null && !newItem.getName().isBlank()) {
            oldItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null && !newItem.getDescription().isBlank()) {
            oldItem.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            oldItem.setAvailable(newItem.getAvailable());
        }
        Item itemStorage = itemRepository.save(oldItem);
        logStorageChanges("Update item", itemStorage.toString());
        return toItemOutShortDto(itemStorage.getId(), itemStorage);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemOutLongDto getItemById(long userId, long itemId) {
        checkUserExists(userRepository, userId);
        checkItemExists(itemRepository, itemId);
        Item itemStorage = itemRepository.getReferenceById(itemId);
        ItemOutLongDto itemOutLongDto;
        if (userId == itemStorage.getUserId()) {
            LocalDateTime now = LocalDateTime.now();
            itemOutLongDto = toItemOutLongDto(itemStorage, getLastBookingDto(itemStorage, now),
                    getNextBookingDto(itemStorage, now), getCommentsDtoForItem(itemStorage));
        } else {
            itemOutLongDto = toItemOutLongDto(itemStorage, null, null,
                    getCommentsDtoForItem(itemStorage));
        }
        return itemOutLongDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemOutLongDto> getItems(long userId) {
        checkUserExists(userRepository, userId);
        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findAllByUserIdOrderById(userId).stream()
                .map(item -> toItemOutLongDto(item, getLastBookingDto(item, now),
                        getNextBookingDto(item, now), getCommentsDtoForItem(item)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemOutShortDto> findByNameOrDescription(long userId, String text) {
        checkUserExists(userRepository, userId);
        if (text != null && !text.isBlank()) {
            String formattedText = text.toLowerCase();
            return itemRepository.findByNameOrDescription(formattedText, formattedText).stream()
                    .map(item -> toItemOutShortDto(item.getId(), item))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public CommentOutDto addComment(long userId, long itemId, CommentInnerDto commentInnerDto) {
        checkUserExists(userRepository, userId);
        checkItemExists(itemRepository, itemId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findAllByBookerAndFinished(itemId, userId, now);
        if (bookings == null || bookings.isEmpty()) {
            throw new ValidationException(String.format("User with id %s did not book the item with id %s " +
                    "or the reservation has not ended yet.", userId, itemId));
        }
        User author = userRepository.getReferenceById(userId);
        Item item = itemRepository.getReferenceById(itemId);
        Comment comment = toComment(commentInnerDto, item, author, now);
        Comment commentStorage = commentRepository.save(comment);
        logStorageChanges("Add comment", commentStorage.toString());
        return toCommentOutDto(commentStorage);
    }


    private BookingForItemDto getLastBookingDto(Item item, LocalDateTime dataTime) {
        BookingForItemDto lastBooking = null;
        Optional<Booking> lastBookingOpt = bookingRepository.findLastByItemId(item.getId(), dataTime);
        if (lastBookingOpt.isPresent()) {
            lastBooking = toBookingForItemDto(lastBookingOpt.get());
        }
        return lastBooking;
    }

    private BookingForItemDto getNextBookingDto(Item item, LocalDateTime dataTime) {
        BookingForItemDto nextBooking = null;
        Optional<Booking> nextBookingOpt = bookingRepository.findNextByItemId(item.getId(), dataTime);
        if (nextBookingOpt.isPresent()) {
            nextBooking = toBookingForItemDto(nextBookingOpt.get());
        }
        return nextBooking;
    }

    private List<CommentOutDto> getCommentsDtoForItem(Item item) {
        List<Comment> comments = commentRepository.findAllByItem(item);
        if (comments != null && !comments.isEmpty()) {
            return comments.stream()
                    .map(CommentsMapper::toCommentOutDto)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
