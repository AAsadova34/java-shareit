package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.comment.dto.CommentOutDto;

import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemOutLongDto {
    @PositiveOrZero(message = "Id must not be negative")
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;
    private List<CommentOutDto> comments;
}
