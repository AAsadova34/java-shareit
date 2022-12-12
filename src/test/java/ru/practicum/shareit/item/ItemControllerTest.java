package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepositoryImpl;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ItemControllerTest {
    UserService userService;
    UserController userController;
    ItemController itemController;

    @BeforeEach
    void initialize() {
        userService = new UserServiceImpl(new UserRepositoryImpl());
        userController = new UserController(userService);
        itemController = new ItemController(new ItemServiceImpl(new ItemRepositoryImpl(), userService));
    }

    @Test
    void addItemTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem = itemController.addItem(newUser.getId(), itemDto);
        itemDto.setId(1);
        assertThat(itemDto, equalTo(newItem));
    }

    @Test
    void addItemWithDuplicateIdTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Item description1")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .id(newItem1.getId())
                .name("Item2")
                .description("Item description2")
                .available(false)
                .build();
        ConflictException e = Assertions.assertThrows(
                ConflictException.class, () -> itemController.addItem(newUser.getId(), itemDto2));
        assertThat(String.format("Item with id %s already exists", itemDto2.getId()), equalTo(e.getMessage()));
    }

    @Test
    void addItemWithFailUserIdTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemController.addItem(-1, itemDto));
        assertThat("User with id -1 not found", equalTo(e.getMessage()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addItemWithNullOrEmptyNameTest(String name) {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto = ItemDto.builder()
                .name(name)
                .description("Item description")
                .available(true)
                .build();
        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> itemController.addItem(newUser.getId(), itemDto));
        assertThat("Name must not be null or empty", equalTo(e.getMessage()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addItemWithNullOrEmptyDescriptionTest(String description) {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto = ItemDto.builder()
                .name("Item1")
                .description(description)
                .available(true)
                .build();
        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> itemController.addItem(newUser.getId(), itemDto));
        assertThat("Description must not be null or empty", equalTo(e.getMessage()));
    }

    @ParameterizedTest
    @NullSource
    void addItemWithNullAvailableTest(Boolean available) {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(available)
                .build();
        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> itemController.addItem(newUser.getId(), itemDto));
        assertThat("Available must not be null", equalTo(e.getMessage()));
    }

    @Test
    void updateItemTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .id(newItem1.getId())
                .name("Item2")
                .description("Item description2")
                .available(false)
                .build();
        ItemDto updateItem = itemController.updateItem(newUser.getId(), newItem1.getId(), itemDto2);
        assertThat(itemDto2, equalTo(updateItem));
    }

    @Test
    void updateItemNameTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .id(newItem1.getId())
                .name("Item2")
                .description("Item description")
                .available(true)
                .build();
        ItemDto updateItem = itemController.updateItem(newUser.getId(), newItem1.getId(), itemDto2);
        assertThat(itemDto2, equalTo(updateItem));
    }

    @Test
    void updateItemDescriptionTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .id(newItem1.getId())
                .name("Item1")
                .description("New item description")
                .available(true)
                .build();
        ItemDto updateItem = itemController.updateItem(newUser.getId(), newItem1.getId(), itemDto2);
        assertThat(itemDto2, equalTo(updateItem));
    }

    @Test
    void updateItemAvailableTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .id(newItem1.getId())
                .name("Item1")
                .description("Item description")
                .available(false)
                .build();
        ItemDto updateItem = itemController.updateItem(newUser.getId(), newItem1.getId(), itemDto2);
        assertThat(itemDto2, equalTo(updateItem));
    }

    @Test
    void updateItemNotOwnerTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto newUser1 = userController.addUser(userDto1);
        UserDto userDto2 = UserDto.builder()
                .email("user2@Ya.ru")
                .name("User2")
                .build();
        UserDto newUser2 = userController.addUser(userDto2);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser1.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .id(newItem1.getId())
                .name("Item2")
                .description("Item description2")
                .available(false)
                .build();
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemController.updateItem(newUser2.getId(), newItem1.getId(), itemDto2));
        assertThat(String.format("The user with id %s cannot change the user with id %s item",
                newUser2.getId(), newItem1.getId()), equalTo(e.getMessage()));
    }

    @Test
    void updateItemFailUserIdTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .id(newItem1.getId())
                .name("Item2")
                .description("Item description2")
                .available(false)
                .build();
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemController.updateItem(-1, itemDto2.getId(), itemDto2));
        assertThat("User with id -1 not found", equalTo(e.getMessage()));
    }

    @Test
    void updateItemFailItemIdTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .id(-1)
                .name("Item2")
                .description("Item description2")
                .available(false)
                .build();
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemController.updateItem(newUser.getId(), itemDto2.getId(), itemDto2));
        assertThat("Item with id -1 not found", equalTo(e.getMessage()));
    }

    @Test
    void getItemByIdTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem = itemController.addItem(newUser.getId(), itemDto);
        assertThat(newItem, equalTo(itemController.getItemById(newUser.getId(), newItem.getId())));
    }

    @Test
    void getItemByIdFailUserIdTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto = ItemDto.builder()
                .name("Item1")
                .description("Item description")
                .available(true)
                .build();
        ItemDto newItem = itemController.addItem(newUser.getId(), itemDto);
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemController.getItemById(-1, newItem.getId()));
        assertThat("User with id -1 not found", equalTo(e.getMessage()));
    }

    @Test
    void getItemByIdFailItemIdTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemController.getItemById(newUser.getId(), -1));
        assertThat("Item with id -1 not found", equalTo(e.getMessage()));
    }

    @Test
    void getItemsTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto newUser1 = userController.addUser(userDto1);
        UserDto userDto2 = UserDto.builder()
                .email("user2@Ya.ru")
                .name("User2")
                .build();
        UserDto newUser2 = userController.addUser(userDto2);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Item description1")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser1.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .name("Item2")
                .description("Item description2")
                .available(false)
                .build();
        ItemDto newItem2 = itemController.addItem(newUser2.getId(), itemDto2);
        assertThat(itemController.getItems(newUser1.getId()), hasItem(newItem1));
        assertThat(itemController.getItems(newUser1.getId()), hasSize(1));
    }

    @Test
    void getItemsEmptyTest() {
        UserDto userDto1 = UserDto.builder()
                .email("user1@Ya.ru")
                .name("User1")
                .build();
        UserDto newUser1 = userController.addUser(userDto1);
        assertThat(itemController.getItems(newUser1.getId()), empty());
    }

    @Test
    void getItemsFailUserIdTest() {
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class, () -> itemController.getItems(-1));
        assertThat("User with id -1 not found", equalTo(e.getMessage()));
    }

    @Test
    void findByNameOrDescriptionTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Перьевая ручка")
                .description("Пиши, как калиграф")
                .available(true)
                .build();
        ItemDto newItem1 = itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .name("Чемодан")
                .description("Чемодан без ручки")
                .available(true)
                .build();
        ItemDto newItem2 = itemController.addItem(newUser.getId(), itemDto2);
        assertThat(itemController.findByNameOrDescription(newUser.getId(), "Ручка"), hasSize(1));
        assertThat(itemController.findByNameOrDescription(newUser.getId(), "Ручка"), hasItem(newItem1));
        assertThat(itemController.findByNameOrDescription(newUser.getId(), "ручк"), hasSize(2));
        assertThat(itemController.findByNameOrDescription(newUser.getId(), "ЧЕМОДАН"), hasSize(1));
        assertThat(itemController.findByNameOrDescription(newUser.getId(), "ЧЕМОДАН"), hasItem(newItem2));
        assertThat(itemController.findByNameOrDescription(newUser.getId(), "пачка"), empty());
    }

    @Test
    void findByNameOrDescriptionAvailableFalseTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);

        ItemDto itemDto1 = ItemDto.builder()
                .name("Перьевая ручка")
                .description("Пиши, как калиграф")
                .available(false)
                .build();
        itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .name("Чемодан")
                .description("Чемодан без ручки")
                .available(false)
                .build();
        itemController.addItem(newUser.getId(), itemDto2);
        assertThat(itemController.findByNameOrDescription(newUser.getId(), "Ручка"), empty());
        assertThat(itemController.findByNameOrDescription(newUser.getId(), "ручк"), empty());
        assertThat(itemController.findByNameOrDescription(newUser.getId(), "ЧЕМОДАН"), empty());
    }

    @Test
    void findByNameOrDescriptionEmptyTest() {
        UserDto userDto = UserDto.builder()
                .email("user@Ya.ru")
                .name("User")
                .build();
        UserDto newUser = userController.addUser(userDto);
        ItemDto itemDto1 = ItemDto.builder()
                .name("Перьевая ручка")
                .description("Пиши, как калиграф")
                .available(true)
                .build();
        itemController.addItem(newUser.getId(), itemDto1);
        ItemDto itemDto2 = ItemDto.builder()
                .name("Чемодан")
                .description("Чемодан без ручки")
                .available(true)
                .build();
        itemController.addItem(newUser.getId(), itemDto2);
        assertThat(itemController.findByNameOrDescription(newUser.getId(), " "), empty());
    }
}
