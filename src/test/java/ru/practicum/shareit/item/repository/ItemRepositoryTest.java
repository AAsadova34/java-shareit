package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.config.Config;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest()
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
@Import(Config.class)
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository requestRepository;

    private User userStorage1;
    private User userStorage2;
    private ItemRequest requestStorage;

    @BeforeEach
    void saveData() {
        User newUser1 = new User()
                .setName("User name1")
                .setEmail("user1@yandex.ru");
        User newUser2 = new User()
                .setName("User name2")
                .setEmail("user2@yandex.ru");
        userStorage1 = userRepository.save(newUser1);
        userStorage2 = userRepository.save(newUser2);

        ItemRequest newRequest1 = new ItemRequest()
                .setDescription("Some item")
                .setRequestor(userStorage2);
        requestStorage = requestRepository.save(newRequest1);
    }

    @Test
    void save_thenItemWithoutRequest_thenSaveItem() {
        Item newItem = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(false);
        Item itemStorage = itemRepository.save(newItem);

        assertThat(itemStorage.getId(), notNullValue());
        assertThat(newItem.getUserId(), equalTo(itemStorage.getUserId()));
        assertThat(newItem.getName(), equalTo(itemStorage.getName()));
        assertThat(newItem.getDescription(), equalTo(itemStorage.getDescription()));
        assertThat(newItem.getAvailable(), equalTo(itemStorage.getAvailable()));
    }

    @Test
    void save_thenItemWithRequest_thenSaveItem() {
        Item newItem = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true)
                .setRequestId(requestStorage.getId());
        Item itemStorage = itemRepository.save(newItem);

        assertThat(itemStorage.getId(), notNullValue());
        assertThat(newItem.getUserId(), equalTo(itemStorage.getUserId()));
        assertThat(newItem.getName(), equalTo(itemStorage.getName()));
        assertThat(newItem.getDescription(), equalTo(itemStorage.getDescription()));
        assertThat(newItem.getAvailable(), equalTo(itemStorage.getAvailable()));
        assertThat(requestStorage.getId(),  equalTo(itemStorage.getRequestId()));
    }

    @Test
    void save_thenItemOwnerIsNotFound_thenDataIntegrityViolationExceptionThrow() {
        Item newItem = new Item()
                .setUserId(-1L)
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);

        DataIntegrityViolationException e = Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> itemRepository.save(newItem));
    }

    @Test
    void getReferenceById_whenItemIsFound_thenReturnItem() {
        Item newItem = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(false);
        Item itemStorage = itemRepository.save(newItem);
        Item actualItem = itemRepository.getReferenceById(itemStorage.getId());

        assertThat(itemStorage.getId(), equalTo(actualItem.getId()));
        assertThat(itemStorage.getUserId(), equalTo(actualItem.getUserId()));
        assertThat(itemStorage.getName(), equalTo(actualItem.getName()));
        assertThat(itemStorage.getDescription(), equalTo(actualItem.getDescription()));
        assertThat(itemStorage.getAvailable(), equalTo(actualItem.getAvailable()));
    }

    @Test
    void existsById_thenItemIsExists_thenReturnTrue() {
        Item newItem = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(false);
        Item itemStorage = itemRepository.save(newItem);

        boolean flag = itemRepository.existsById(itemStorage.getId());

        assertTrue(flag);
    }

    @Test
    void existsById_thenItemIsNotExists_thenReturnFalse() {
        boolean flag = itemRepository.existsById(-1L);

        assertFalse(flag);
    }

    @Test
    void findAllByUserIdOrderById_whenPageableIsSet_thenReturnLimitedList() {
        Item newItem = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(false);
        Item itemStorage1 = itemRepository.save(newItem);
        PageRequest pageable = PageRequest.of(0, 1);

        List<Item> items = itemRepository.findAllByUserIdOrderById(userStorage1.getId(), pageable);

        assertThat(items, hasSize(1));
        assertThat(items, hasItem(itemStorage1));
    }

    @Test
    void findAllByUserIdOrderById_whenPageableIsNotSet_thenReturnFullList() {
        Item newItem1 = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item name1")
                .setDescription("Item description1")
                .setAvailable(false);
        Item newItem2 = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item name2")
                .setDescription("Item description2")
                .setAvailable(true);
        Item itemStorage1 = itemRepository.save(newItem1);
        Item itemStorage2 = itemRepository.save(newItem2);

        List<Item> items = itemRepository.findAllByUserIdOrderById(userStorage1.getId());

        assertThat(items, hasSize(2));
        assertThat(items, hasItem(itemStorage1));
        assertThat(items, hasItem(itemStorage2));
        assertTrue(items.get(0).getId() < items.get(1).getId());
    }

    @Test
    void findAllByUserIdOrderById_whenItemsAreNotExists_thenReturnEmptyList() {
        PageRequest pageable = PageRequest.of(0, 1);

        List<Item> items = itemRepository.findAllByUserIdOrderById(userStorage1.getId(), pageable);

        assertThat(items, empty());
    }

    @Test
    void findByNameOrDescription_whenEveryoneIsAvailable_thenReturnAll() {
        Item newItem1 = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item first")
                .setDescription("Item description")
                .setAvailable(true);
        Item newItem2 = new Item()
                .setUserId(userStorage2.getId())
                .setName("Item second")
                .setDescription("Second in order, but first in importance")
                .setAvailable(true);
        itemRepository.save(newItem1);
        itemRepository.save(newItem2);

        List<Item> items = itemRepository.findByNameOrDescription("first", "first");

        assertThat(items, hasSize(2));
    }

    @Test
    void findByNameOrDescription_whenNotEveryoneIsAvailable_thenReturnOnlyAvailable() {
        Item newItem1 = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item first")
                .setDescription("Item description")
                .setAvailable(false);
        Item newItem2 = new Item()
                .setUserId(userStorage2.getId())
                .setName("Item second")
                .setDescription("Second in order, but first in importance")
                .setAvailable(true);
        Item itemStorage1 = itemRepository.save(newItem1);
        Item itemStorage2 = itemRepository.save(newItem2);

        List<Item> items = itemRepository.findByNameOrDescription("first", "first");

        assertThat(items, hasSize(1));
        assertThat(items, hasItem(itemStorage2));
    }

    @Test
    void findByNameOrDescription_whenPageableIsSet_thenReturnLimitedList() {
        Item newItem1 = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item first")
                .setDescription("Item description")
                .setAvailable(true);
        Item newItem2 = new Item()
                .setUserId(userStorage2.getId())
                .setName("Item second")
                .setDescription("Second in order, but first in importance")
                .setAvailable(true);
        Item newItem3 = new Item()
                .setUserId(userStorage2.getId())
                .setName("Item third")
                .setDescription("Third in order, but first in importance")
                .setAvailable(true);
        itemRepository.save(newItem1);
        itemRepository.save(newItem2);
        itemRepository.save(newItem3);
        PageRequest pageable = PageRequest.of(0, 2);

        List<Item> items = itemRepository.findByNameOrDescription("first", "first", pageable);

        assertThat(items, hasSize(2));
    }

    @Test
    void findItemByRequestId_whenItemIsFound_thenReturnItems() {
        Item newItem1 = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item name1")
                .setDescription("Item description1")
                .setAvailable(false)
                .setRequestId(requestStorage.getId());
        Item newItem2 = new Item()
                .setUserId(userStorage1.getId())
                .setName("Item name2")
                .setDescription("Item description2")
                .setAvailable(true);
        Item itemStorage1 = itemRepository.save(newItem1);
        Item itemStorage2 = itemRepository.save(newItem2);

        List<Item> items = itemRepository.findItemByRequestId(requestStorage.getId());

        assertThat(items, hasSize(1));
        assertThat(items, hasItem(itemStorage1));
    }
}