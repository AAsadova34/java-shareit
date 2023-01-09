package ru.practicum.shareit.booking.dto;

import lombok.*;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingInnerDto {
    @NonNull
    @PositiveOrZero(message = "Id must not be negative")
    private Long itemId;

    @NonNull
    @FutureOrPresent
    private LocalDateTime start;

    @NonNull
    @Future
    private LocalDateTime end;
}
