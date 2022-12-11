package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.PositiveOrZero;

@Data
@Builder
public class UserDto {
    @PositiveOrZero(message = "Id must not be negative")
    private long id;
    private String name;
    @Email(message = "Invalid email format")
    private String email;
}
