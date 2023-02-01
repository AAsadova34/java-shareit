package ru.practicum.shareit.request.repository;

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
import ru.practicum.shareit.request.model.ItemRequest;
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
class ItemRequestRepositoryTest {
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;

    private User requestorStorage;
    private User notRequestorStorage;

    @BeforeEach
    void saveData() {
        User requestor = new User()
                .setName("Requestor name")
                .setEmail("requestor@yandex.ru");
        requestorStorage = userRepository.save(requestor);
        User notRequestor = new User()
                .setName("Not requestor name")
                .setEmail("notrequestor@yandex.ru");
        notRequestorStorage = userRepository.save(notRequestor);
    }

    @Test
    void save_whenRequestIsValid_thenSaveRequest() {
        ItemRequest newRequest = new ItemRequest()
                .setDescription("Some item")
                .setRequestor(requestorStorage);
        ItemRequest requestStorage = requestRepository.save(newRequest);

        assertThat(requestStorage.getId(), notNullValue());
        assertThat(newRequest.getDescription(), equalTo(requestStorage.getDescription()));
        assertThat(newRequest.getRequestor(), equalTo(requestStorage.getRequestor()));
        assertThat(requestStorage.getCreated(), notNullValue());
    }

    @Test
    void save_whenRequestorIsNotFound_thenDataIntegrityViolationExceptionThrow() {
        ItemRequest newRequest = new ItemRequest()
                .setDescription(null)
                .setRequestor(requestorStorage);

        DataIntegrityViolationException e = Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> requestRepository.save(newRequest));
    }

    @Test
    void save_whenDescriptionIsNull_thenDataIntegrityViolationExceptionThrow() {
        ItemRequest newRequest = new ItemRequest()
                .setDescription("Some item")
                .setRequestor(new User().setId(-1L));

        DataIntegrityViolationException e = Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> requestRepository.save(newRequest));
    }

    @Test
    void existsById_whenRequestIsFound_thenReturnTrue() {
        ItemRequest newRequest = new ItemRequest()
                .setDescription("Some item")
                .setRequestor(requestorStorage);
        ItemRequest requestStorage = requestRepository.save(newRequest);

        boolean flag = requestRepository.existsById(requestStorage.getId());

        assertTrue(flag);
    }

    @Test
    void existsById_thenRequestIsNotExists_thenReturnFalse() {
        boolean flag = requestRepository.existsById(-1L);

        assertFalse(flag);
    }

    @Test
    void getReferenceById() {
        ItemRequest newRequest = new ItemRequest()
                .setDescription("Some item")
                .setRequestor(requestorStorage);
        ItemRequest requestStorage = requestRepository.save(newRequest);
        ItemRequest actualRequest = requestRepository.getReferenceById(requestStorage.getId());

        assertThat(requestStorage.getId(), equalTo(actualRequest.getId()));
        assertThat(requestStorage.getDescription(), equalTo(actualRequest.getDescription()));
        assertThat(requestStorage.getRequestor(), equalTo(actualRequest.getRequestor()));
        assertThat(requestStorage.getCreated(), equalTo(actualRequest.getCreated()));
    }

    @Test
    void findItemRequestByRequestorIdOrderByCreatedDesc_whenRequestsAreExists_thenReturnRequests() {
        ItemRequest newRequest1 = new ItemRequest()
                .setDescription("Some item1")
                .setRequestor(requestorStorage);
        ItemRequest newRequest2 = new ItemRequest()
                .setDescription("Some item2")
                .setRequestor(requestorStorage);
        ItemRequest requestStorage1 = requestRepository.save(newRequest1);
        ItemRequest requestStorage2 = requestRepository.save(newRequest2);

        List<ItemRequest> requests = requestRepository
                .findItemRequestByRequestorIdOrderByCreatedDesc(requestorStorage.getId());

        assertThat(requests, hasSize(2));
        assertTrue(requests.get(0).getCreated().isAfter(requests.get(1).getCreated()));
    }

    @Test
    void findItemRequestByRequestorIdOrderByCreatedDesc_whenRequestsNotAreExists_thenReturnEmptyList() {
        ItemRequest newRequest1 = new ItemRequest()
                .setDescription("Some item1")
                .setRequestor(requestorStorage);
        ItemRequest newRequest2 = new ItemRequest()
                .setDescription("Some item2")
                .setRequestor(requestorStorage);
        ItemRequest requestStorage1 = requestRepository.save(newRequest1);
        ItemRequest requestStorage2 = requestRepository.save(newRequest2);

        List<ItemRequest> requests = requestRepository
                .findItemRequestByRequestorIdOrderByCreatedDesc(notRequestorStorage.getId());

        assertThat(requests, empty());
    }

    @Test
    void findAllOtherRequests_whenNoOtherRequestsExist_thenReturnRequests() {
        ItemRequest newRequest1 = new ItemRequest()
                .setDescription("Some item1")
                .setRequestor(requestorStorage);
        ItemRequest newRequest2 = new ItemRequest()
                .setDescription("Some item2")
                .setRequestor(requestorStorage);
        ItemRequest requestStorage1 = requestRepository.save(newRequest1);
        ItemRequest requestStorage2 = requestRepository.save(newRequest2);

        List<ItemRequest> requests = requestRepository
                .findAllOtherRequests(requestorStorage.getId());

        assertThat(requests, empty());
    }

    @Test
    void findAllOtherRequests_whenPageableIsNotSet_thenReturnFullList() {
        ItemRequest newRequest1 = new ItemRequest()
                .setDescription("Some item1")
                .setRequestor(requestorStorage);
        ItemRequest newRequest2 = new ItemRequest()
                .setDescription("Some item2")
                .setRequestor(requestorStorage);
        ItemRequest requestStorage1 = requestRepository.save(newRequest1);
        ItemRequest requestStorage2 = requestRepository.save(newRequest2);

        List<ItemRequest> requests = requestRepository
                .findAllOtherRequests(notRequestorStorage.getId());

        assertThat(requests, hasSize(2));
        assertTrue(requests.get(0).getCreated().isAfter(requests.get(1).getCreated()));
    }

    @Test
    void findAllOtherRequests_whenPageableIsSet_thenReturnLimitedList() {
        ItemRequest newRequest1 = new ItemRequest()
                .setDescription("Some item1")
                .setRequestor(requestorStorage);
        ItemRequest newRequest2 = new ItemRequest()
                .setDescription("Some item2")
                .setRequestor(requestorStorage);
        ItemRequest requestStorage1 = requestRepository.save(newRequest1);
        ItemRequest requestStorage2 = requestRepository.save(newRequest2);
        PageRequest pageable = PageRequest.of(0, 1);

        List<ItemRequest> requests = requestRepository
                .findAllOtherRequests(notRequestorStorage.getId(), pageable);

        assertThat(requests, hasSize(1));
        assertThat(requests, hasItem(requestStorage2));
    }
}