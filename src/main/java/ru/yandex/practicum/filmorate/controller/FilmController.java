package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateFilmExtraRules(film);

        film.setId(nextId++);
        films.put(film.getId(), film);

        log.info("Создан фильм: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(film.getId())) {
            // Postman допускает 500 или 404 — делаем правильно: 404
            throw new NoSuchElementException("Фильм не найден");
        }

        validateFilmExtraRules(film);

        films.put(film.getId(), film);
        log.info("Обновлён фильм: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    private void validateFilmExtraRules(Film film) {
        String desc = film.getDescription();
        if (desc != null && desc.length() > 200) {
            log.warn("Ошибка валидации фильма: description > 200");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        LocalDate release = film.getReleaseDate();
        if (release == null) {
            log.warn("Ошибка валидации фильма: releaseDate is null");
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (release.isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Ошибка валидации фильма: releaseDate < {}", CINEMA_BIRTHDAY);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NoSuchElementException e) {
        return Map.of("error", e.getMessage());
    }
}
