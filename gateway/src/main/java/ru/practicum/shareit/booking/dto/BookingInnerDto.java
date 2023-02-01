package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BookingInnerDto {

    @NotNull(message = "Id must not be null")
    @PositiveOrZero(message = "Id must not be negative")
    private Long itemId;

    @NotNull(message = "The start must not be null")
    @FutureOrPresent(message = "The start must be in the present or future")
    private LocalDateTime start;

    @NotNull(message = "The end must not be null")
    @Future(message = "The end must be in the future")
    private LocalDateTime end;
}
