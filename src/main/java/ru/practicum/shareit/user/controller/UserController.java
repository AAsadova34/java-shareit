package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.log.Logger.logRequest;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        logRequest(HttpMethod.POST, "/users", "no", userDto.toString());
        return userService.addUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable long id, @Valid @RequestBody UserDto userDto) {
        logRequest(HttpMethod.PATCH, "/users/" + id, "no", userDto.toString());
        return userService.updateUser(id, userDto);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable long id) {
        logRequest(HttpMethod.GET, "/users/" + id, "no", "no");
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void delUserById(@PathVariable long id) {
        logRequest(HttpMethod.DELETE, "/users/" + id, "no", "no");
        userService.delUserById(id);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        logRequest(HttpMethod.GET, "/users", "no", "no");
        return userService.getUsers();
    }
}
