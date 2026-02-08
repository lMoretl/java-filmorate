package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAll() {
        return List.copyOf(filmStorage.getAll());
    }

    public Film getById(long id) {
        return filmStorage.getById(id);
    }

    public Film create(Film film) {
        validateFilmExtra(film);
        Film created = filmStorage.create(film);
        log.info("Создан фильм id={}, name={}", created.getId(), created.getName());
        return created;
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        validateFilmExtra(film);
        Film updated = filmStorage.update(film);
        log.info("Обновлён фильм id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        boolean added = film.getLikes().add(userId);
        filmStorage.update(film);

        log.info("Лайк фильму {} от пользователя {} (added={})", filmId, userId, added);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        boolean removed = film.getLikes().remove(userId);
        filmStorage.update(film);

        log.info("Удаление лайка фильму {} от пользователя {} (removed={})", filmId, userId, removed);
    }

    public List<Film> getPopular(int count) {
        int limit = (count <= 0) ? 10 : count;

        return filmStorage.getAll().stream()
                .sorted(Comparator
                        .comparingInt((Film f) -> f.getLikes() == null ? 0 : f.getLikes().size())
                        .reversed()
                        .thenComparing(Film::getId))
                .limit(limit)
                .toList();
    }

    private void validateFilmExtra(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }
}
