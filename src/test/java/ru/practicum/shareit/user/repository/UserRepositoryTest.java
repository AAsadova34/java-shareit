package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private final User newUser1 = new User()
            .setName("User name1")
            .setEmail("user1@yandex.ru");

    private final User newUser2 = new User()
            .setName("User name2")
            .setEmail("user2@yandex.ru");

    @Test
    void save_whenUserIsValid_thenSaveUser() {
        User userStorage = userRepository.save(newUser1);

        assertThat(userStorage.getId(), notNullValue());
        assertThat(newUser1.getName(), equalTo(userStorage.getName()));
        assertThat(newUser1.getEmail(), equalTo(userStorage.getEmail()));
    }

    @Test
    void save_whenUserIsSavedAgain_thenUpdateUser() {
        User userStorage1 = userRepository.save(newUser1);
        userStorage1.setName("New name user1");
        userRepository.save(userStorage1);

        assertThat(userRepository.findAll(), hasSize(1));
    }

    @Test
    void save_whenNonUniqueEmail_thenDataIntegrityViolationExceptionThrow() {
        User userStorage1 = userRepository.save(newUser1);
        newUser2.setEmail(userStorage1.getEmail());

        DataIntegrityViolationException e = Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> userRepository.save(newUser2));
    }

    @Test
    void getReferenceById_whenUserIsFound_thenReturnUser() {
        User userStorage = userRepository.save(newUser1);
        User actualUser = userRepository.getReferenceById(userStorage.getId());

        assertThat(userStorage.getId(), equalTo(actualUser.getId()));
        assertThat(userStorage.getName(), equalTo(actualUser.getName()));
        assertThat(userStorage.getEmail(), equalTo(actualUser.getEmail()));
    }

    @Test
    void existsById_thenUserIsExists_thenReturnTrue() {
        User userStorage = userRepository.save(newUser1);
        boolean flag = userRepository.existsById(userStorage.getId());

        assertTrue(flag);
    }

    @Test
    void existsById_thenUserIsNotExists_thenReturnFalse() {
        boolean flag = userRepository.existsById(-1L);

        assertFalse(flag);
    }

    @Test
    void deleteById_whenUserIsFound_thenDeleteUser() {
        User userStorage = userRepository.save(newUser1);
        boolean flagTrue = userRepository.existsById(userStorage.getId());

        userRepository.deleteById(userStorage.getId());
        boolean flagFalse = userRepository.existsById(userStorage.getId());

        assertTrue(flagTrue);
        assertFalse(flagFalse);
    }

    @Test
    void deleteById_whenUserIsNotFound_thenEmptyResultDataAccessExceptionThrow() {
        EmptyResultDataAccessException e = Assertions.assertThrows(
                EmptyResultDataAccessException.class, () -> userRepository.deleteById(-1));
        assertThat("No class ru.practicum.shareit.user.model.User entity with id -1 exists!",
                equalTo(e.getMessage()));
    }

    @Test
    void findAll_whenUsersExists_thenReturnUsers() {
        userRepository.save(newUser1);
        userRepository.save(newUser2);

        assertThat(userRepository.findAll(), hasSize(2));
    }

    @Test
    void findAll_whenUsersNotExists_thenReturnEmptyList() {
        assertThat(userRepository.findAll(), hasSize(0));
    }
}