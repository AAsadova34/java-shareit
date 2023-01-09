package ru.practicum.shareit.item.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
        Comment save(Comment comment);

    List<Comment> findAllByItem(Item item);
}
