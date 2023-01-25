package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.practicum.shareit.item.dto.ItemOutShortDto;

import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ItemRequestOutLongDto {
    @PositiveOrZero(message = "Id must not be negative")
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemOutShortDto> items;
}
