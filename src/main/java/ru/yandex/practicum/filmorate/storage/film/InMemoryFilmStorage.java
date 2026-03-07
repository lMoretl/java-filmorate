package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Long id = film.getId();
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (!films.containsKey(id)) {
            throw new NoSuchElementException("Фильм не найден");
        }
        films.put(id, film);
        return film;
    }

    @Override
    public Film getById(long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NoSuchElementException("Фильм не найден");
        }
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public void delete(long id) {
        if (!films.containsKey(id)) {
            throw new NoSuchElementException("Фильм не найден");
        }
        films.remove(id);
    }

    @Override
    public void addLike(long filmId, long userId) {
        Film film = getById(filmId);
        film.getLikes().add(userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        Film film = getById(filmId);
        film.getLikes().remove(userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        return films.values().stream()
                .sorted(Comparator
                        .comparingInt((Film film) -> film.getLikes().size())
                        .reversed()
                        .thenComparing(Film::getId))
                .limit(count)
                .toList();
    }
}