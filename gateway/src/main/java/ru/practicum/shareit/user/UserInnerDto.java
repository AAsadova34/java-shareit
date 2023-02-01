package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserInnerDto {

    private String name;

    @Email(message = "Invalid email format")
    private String email;
}
