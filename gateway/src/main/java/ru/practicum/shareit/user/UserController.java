package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static ru.practicum.shareit.log.Logger.logRequest;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> addUser(@Valid @RequestBody UserInnerDto userInnerDto) {
        logRequest(HttpMethod.POST, "/users", "no", userInnerDto.toString());
        return userClient.addUser(userInnerDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable long id, @Valid @RequestBody UserInnerDto userInnerDto) {
        logRequest(HttpMethod.PATCH, "/users/" + id, "no", userInnerDto.toString());
        return userClient.updateUser(id, userInnerDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable long id) {
        logRequest(HttpMethod.GET, "/users/" + id, "no", "no");
        return userClient.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delUserById(@PathVariable long id) {
        logRequest(HttpMethod.DELETE, "/users/" + id, "no", "no");
        return userClient.delUserById(id);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        logRequest(HttpMethod.GET, "/users", "no", "no");
        return userClient.getUsers();
    }
}
