package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserValidator {

    public static void validate(User user) {
        if (user == null) {
            throw new ValidationException("Пустое тело запроса");
        }

        String email = user.getEmail();
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new ValidationException("Электронная почта должна быть указана и содержать символ @");
        }

        String login = user.getLogin();
        if (login == null || login.trim().isEmpty()) {
            throw new ValidationException("Логин не может быть пустым");
        }
        if (login.contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }

        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения не указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        // name может быть пустым -> используем login
        String name = user.getName();
        if (name == null || name.trim().isEmpty()) {
            user.setName(login);
        }
    }
}
