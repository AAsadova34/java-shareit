package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BookingForItemDto {
    private Long id;
    private Long itemId;
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
}
