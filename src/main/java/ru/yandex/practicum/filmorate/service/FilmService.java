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
        validateReleaseDate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        validateReleaseDate(film);
        return filmStorage.update(film);
    }

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        film.getLikes().remove(userId);
        filmStorage.update(film);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator
                        .comparingInt((Film film) -> film.getLikes().size())
                        .reversed()
                        .thenComparing(Film::getId))
                .limit(count)
                .toList();
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException(
                    "Дата релиза — не раньше 28 декабря 1895 года"
            );
        }
    }
}
