package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserValidator {

    public static void validate(User user) {
        if (user == null) {
            throw new ValidationException("Пустое тело запроса");
        }

        String login = user.getLogin();
        if (login != null && login.contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }

        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения не указана");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        String name = user.getName();
        if (name == null || name.isBlank()) {
            user.setName(login);
        }
    }
}
