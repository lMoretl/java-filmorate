package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.FilmValidator;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {

    @Test
    void filmValidation_shouldFail_whenNameBlank() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("ok");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
        assertTrue(ex.getMessage().toLowerCase().contains("название"));
    }

    @Test
    void filmValidation_shouldFail_whenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Matrix");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
        assertTrue(ex.getMessage().toLowerCase().contains("200"));
    }

    @Test
    void filmValidation_shouldFail_whenReleaseDateBeforeCinemaBirthday() {
        Film film = new Film();
        film.setName("Old");
        film.setDescription("ok");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
        assertTrue(ex.getMessage().toLowerCase().contains("1895"));
    }

    @Test
    void filmValidation_shouldFail_whenDurationNotPositive() {
        Film film = new Film();
        film.setName("Ok");
        film.setDescription("ok");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        ValidationException ex = assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
        assertTrue(ex.getMessage().toLowerCase().contains("положительным"));
    }


    @Test
    void userValidation_shouldFail_whenEmailInvalid() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> UserValidator.validate(user));
        assertTrue(ex.getMessage().toLowerCase().contains("@"));
    }

    @Test
    void userValidation_shouldFail_whenLoginHasSpaces() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setLogin("bad login");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> UserValidator.validate(user));
        assertTrue(ex.getMessage().toLowerCase().contains("пробел"));
    }

    @Test
    void userValidation_shouldSetNameFromLogin_whenNameBlank() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setLogin("login123");
        user.setName("   ");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertDoesNotThrow(() -> UserValidator.validate(user));
        assertEquals("login123", user.getName());
    }

    @Test
    void userValidation_shouldFail_whenBirthdayInFuture() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException ex = assertThrows(ValidationException.class, () -> UserValidator.validate(user));
        assertTrue(ex.getMessage().toLowerCase().contains("будущ"));
    }
}
