package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BookingInnerDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}
