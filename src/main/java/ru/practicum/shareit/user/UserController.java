package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.log.Logger;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        Logger.logRequest(HttpMethod.POST, "/users", "no", userDto.toString());
        return userService.addUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable long id, @Valid @RequestBody UserDto userDto) {
        Logger.logRequest(HttpMethod.PATCH, "/users/" + id, "no", userDto.toString());
        return userService.updateUser(id, userDto);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable long id) {
        Logger.logRequest(HttpMethod.GET, "/users/" + id, "no", "no");
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void delUserById(@PathVariable long id) {
        Logger.logRequest(HttpMethod.DELETE, "/users/" + id, "no", "no");
        userService.delUserById(id);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        Logger.logRequest(HttpMethod.GET, "/users", "no", "no");
        return userService.getUsers();
    }
}
