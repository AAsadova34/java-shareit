package ru.practicum.shareit.item.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CommentInnerDto {
    @NotBlank(message = "Text comment text must not be empty")
    private String text;
}
