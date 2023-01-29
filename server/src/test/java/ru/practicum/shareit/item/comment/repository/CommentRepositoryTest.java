package ru.practicum.shareit.item.comment.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.config.Config;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
class CommentRepositoryTest {
    @Autowired
    private  CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    User ownerStorage;
    User authorStorage;
    Item itemStorage;
    Item extraItemStorage;

    @BeforeEach
    void saveData() {
        User owner = new User()
                .setName("Owner")
                .setEmail("owner@yandex.ru");
        User author = new User()
                .setName("Author")
                .setEmail("author@yandex.ru");
        ownerStorage = userRepository.save(owner);
        authorStorage = userRepository.save(author);

        Item newItem = new Item()
                .setUserId(ownerStorage.getId())
                .setName("Item name")
                .setDescription("Item description")
                .setAvailable(true);
        Item extraItem = new Item()
                .setUserId(ownerStorage.getId())
                .setName("Extra item name")
                .setDescription("Extra item description")
                .setAvailable(true);
        itemStorage = itemRepository.save(newItem);
        extraItemStorage = itemRepository.save(extraItem);
    }

    @Test
    void save_whenCommentIsValid_thenSaveComment() {
        Comment newComment = new Comment()
                .setText("Great item")
                .setItem(itemStorage)
                .setAuthor(authorStorage);
        Comment commentStorage = commentRepository.save(newComment);

        assertThat(commentStorage.getId(), notNullValue());
        assertThat(newComment.getText(), equalTo(commentStorage.getText()));
        assertThat(newComment.getItem(), equalTo(commentStorage.getItem()));
        assertThat(newComment.getAuthor(), equalTo(commentStorage.getAuthor()));
        assertThat(commentStorage.getCreated(), notNullValue());
    }

    @Test
    void save_whenItemIsNotFound_thenDataIntegrityViolationExceptionThrow() {
        Comment newComment = new Comment()
                .setText("Great item")
                .setItem(new Item().setId(-1L))
                .setAuthor(authorStorage);

        DataIntegrityViolationException e = Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> commentRepository.save(newComment));
    }

    @Test
    void save_whenAuthorIsNotFound_thenDataIntegrityViolationExceptionThrow() {
        Comment newComment = new Comment()
                .setText("Great item")
                .setItem(itemStorage)
                .setAuthor(new User().setId(-1L));

        DataIntegrityViolationException e = Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> commentRepository.save(newComment));
    }

    @Test
    void save_whenTextIsNull_thenDataIntegrityViolationExceptionThrow() {
        Comment newComment = new Comment()
                .setText(null)
                .setItem(itemStorage)
                .setAuthor(authorStorage);

        DataIntegrityViolationException e = Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> commentRepository.save(newComment));
    }

    @Test
    void findAllByItem_whenCommentsAreExists_ReturnComments() {
        Comment newComment1 = new Comment()
                .setText("Great item")
                .setItem(itemStorage)
                .setAuthor(authorStorage);
        Comment newComment2 = new Comment()
                .setText("Great item")
                .setItem(extraItemStorage)
                .setAuthor(authorStorage);
        Comment commentStorage1 = commentRepository.save(newComment1);
        Comment commentStorage2 = commentRepository.save(newComment2);

        List<Comment> comments = commentRepository.findAllByItemId(itemStorage.getId());

        assertThat(comments, hasSize(1));
        assertThat(comments, hasItem(commentStorage1));
    }

    @Test
    void existsById_thenCommentIsExists_thenReturnTrue() {
        Comment newComment = new Comment()
                .setText("Great item")
                .setItem(itemStorage)
                .setAuthor(authorStorage);
        Comment commentStorage = commentRepository.save(newComment);

        boolean flag = commentRepository.existsById(commentStorage.getId());

        assertTrue(flag);
    }

    @Test
    void existsById_thenCommentIsNotExists_thenReturnFalse() {
        boolean flag = commentRepository.existsById(-1L);

        assertFalse(flag);
    }
}