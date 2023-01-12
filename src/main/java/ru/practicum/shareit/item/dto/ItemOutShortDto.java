package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.PositiveOrZero;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemOutShortDto {
    @PositiveOrZero(message = "Id must not be negative")
    private Long id;
    private String name;
    private String description;
    private Boolean available;
}
