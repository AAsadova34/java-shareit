package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class ItemDto {
    @PositiveOrZero(message = "Id must not be negative")
    private long id;
    private String name;
    private String description;
    private Boolean available;
}
