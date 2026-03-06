package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public List<Film> getAll() {
        return List.copyOf(filmStorage.getAll());
    }

    public Film getById(long id) {
        return filmStorage.getById(id);
    }

    public Film create(Film film) {
        validateReleaseDate(film);
        validateMpaAndGenres(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        validateReleaseDate(film);
        validateMpaAndGenres(film);

        return filmStorage.update(film);
    }

    public void addLike(long filmId, long userId) {
        filmStorage.getById(filmId);
        userStorage.getById(userId);

        jdbcTemplate.update(
                "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)",
                filmId,
                userId
        );
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.getById(filmId);
        userStorage.getById(userId);

        jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                filmId,
                userId
        );
    }

    public List<Film> getPopular(int count) {
        int limit = count > 0 ? count : 10;

        List<Long> filmIds = jdbcTemplate.query(
                """
                SELECT f.id
                FROM films f
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                GROUP BY f.id
                ORDER BY COUNT(fl.user_id) DESC, f.id ASC
                LIMIT ?
                """,
                (rs, rowNum) -> rs.getLong("id"),
                limit
        );

        return filmIds.stream()
                .map(filmStorage::getById)
                .toList();
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private void validateMpaAndGenres(Film film) {

        Integer mpaId = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mpa WHERE id = ?",
                Integer.class,
                film.getMpa().getId()
        );

        if (mpaId == null || mpaId == 0) {
            throw new java.util.NoSuchElementException("MPA рейтинг не найден");
        }

        if (film.getGenres() != null) {
            for (var genre : film.getGenres()) {

                Integer genreExists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM genres WHERE id = ?",
                        Integer.class,
                        genre.getId()
                );

                if (genreExists == null || genreExists == 0) {
                    throw new java.util.NoSuchElementException("Жанр не найден");
                }
            }
        }
    }
}