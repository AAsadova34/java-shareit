package ru.practicum.shareit.item.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.comment.dto.CommentInnerDto;
import ru.practicum.shareit.item.comment.dto.CommentOutDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class CommentsMapper {
    public static Comment toComment(CommentInnerDto commentInnerDto, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(commentInnerDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        return comment;
    }

    public static CommentOutDto toCommentOutDto(Comment comment) {
        CommentOutDto commentOutDto = new CommentOutDto();
        commentOutDto.setId(comment.getId());
        commentOutDto.setText(comment.getText());
        commentOutDto.setAuthorName(comment.getAuthor().getName());
        commentOutDto.setCreated(comment.getCreated());
        return commentOutDto;
    }
}
