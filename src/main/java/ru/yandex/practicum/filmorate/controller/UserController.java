package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        UserValidator.validate(user);

        user.setId(nextId++);
        users.put(user.getId(), user);

        log.info("Создан пользователь: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        UserValidator.validate(user);

        users.put(user.getId(), user);
        log.info("Обновлён пользователь: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }
}
