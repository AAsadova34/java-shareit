package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.PositiveOrZero;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @PositiveOrZero(message = "Id must not be negative")
    private Long id;

    private String name;

    @Email(message = "Invalid email format")
    private String email;
}
