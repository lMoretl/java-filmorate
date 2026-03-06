package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

@Primary
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        saveGenres(film);

        return getById(film.getId());
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);

        return getById(film.getId());
    }

    @Override
    public Film getById(long id) {
        List<Film> films = jdbcTemplate.query(
                """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       m.id   AS mpa_id,
                       m.name AS mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                WHERE f.id = ?
                """,
                (rs, rowNum) -> {
                    Film film = new Film();
                    film.setId(rs.getLong("id"));
                    film.setName(rs.getString("name"));
                    film.setDescription(rs.getString("description"));
                    film.setReleaseDate(rs.getDate("release_date").toLocalDate());
                    film.setDuration(rs.getInt("duration"));
                    film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
                    film.setGenres(loadGenres(rs.getLong("id")));
                    film.setLikes(loadLikes(rs.getLong("id")));
                    return film;
                },
                id
        );

        if (films.isEmpty()) {
            throw new NoSuchElementException("Фильм не найден");
        }

        return films.get(0);
    }

    @Override
    public Collection<Film> getAll() {
        return jdbcTemplate.query(
                """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       m.id   AS mpa_id,
                       m.name AS mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                ORDER BY f.id
                """,
                (rs, rowNum) -> {
                    Film film = new Film();
                    film.setId(rs.getLong("id"));
                    film.setName(rs.getString("name"));
                    film.setDescription(rs.getString("description"));
                    film.setReleaseDate(rs.getDate("release_date").toLocalDate());
                    film.setDuration(rs.getInt("duration"));
                    film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
                    film.setGenres(loadGenres(rs.getLong("id")));
                    film.setLikes(loadLikes(rs.getLong("id")));
                    return film;
                }
        );
    }

    @Override
    public void delete(long id) {
        getById(id);
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(
                    "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)",
                    film.getId(),
                    genre.getId()
            );
        }
    }

    private Set<Genre> loadGenres(long filmId) {
        List<Genre> genres = jdbcTemplate.query(
                """
                SELECT g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                filmId
        );

        return new LinkedHashSet<>(genres);
    }

    private Set<Long> loadLikes(long filmId) {
        List<Long> likes = jdbcTemplate.query(
                """
                SELECT user_id
                FROM film_likes
                WHERE film_id = ?
                ORDER BY user_id
                """,
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId
        );

        return new LinkedHashSet<>(likes);
    }
}