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

    private static final String INSERT_FILM =
            "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_FILM =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String DELETE_FILM =
            "DELETE FROM films WHERE id = ?";

    private static final String DELETE_FILM_GENRES =
            "DELETE FROM film_genres WHERE film_id = ?";

    private static final String INSERT_FILM_GENRE =
            "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";

    private static final String FIND_FILM_BY_ID = """
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
            """;

    private static final String FIND_ALL_FILMS = """
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
            """;

    private static final String FIND_FILM_GENRES = """
            SELECT g.id, g.name
            FROM film_genres fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE fg.film_id = ?
            ORDER BY g.id
            """;

    private static final String FIND_FILM_LIKES = """
            SELECT user_id
            FROM film_likes
            WHERE film_id = ?
            ORDER BY user_id
            """;

    private static final String ADD_LIKE =
            "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE =
            "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String FIND_POPULAR_FILM_IDS = """
            SELECT f.id
            FROM films f
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY COUNT(fl.user_id) DESC, f.id ASC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_FILM, new String[]{"id"});
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

        jdbcTemplate.update(UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());
        saveGenres(film);

        return getById(film.getId());
    }

    @Override
    public Film getById(long id) {
        List<Film> films = jdbcTemplate.query(FIND_FILM_BY_ID,
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
        return jdbcTemplate.query(FIND_ALL_FILMS,
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
        jdbcTemplate.update(DELETE_FILM, id);
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbcTemplate.update(ADD_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbcTemplate.update(REMOVE_LIKE, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        List<Long> filmIds = jdbcTemplate.query(
                FIND_POPULAR_FILM_IDS,
                (rs, rowNum) -> rs.getLong("id"),
                count
        );

        return filmIds.stream()
                .map(this::getById)
                .toList();
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(INSERT_FILM_GENRE, film.getId(), genre.getId());
        }
    }

    private Set<Genre> loadGenres(long filmId) {
        List<Genre> genres = jdbcTemplate.query(
                FIND_FILM_GENRES,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                filmId
        );

        return new LinkedHashSet<>(genres);
    }

    private Set<Long> loadLikes(long filmId) {
        List<Long> likes = jdbcTemplate.query(
                FIND_FILM_LIKES,
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId
        );

        return new LinkedHashSet<>(likes);
    }
}