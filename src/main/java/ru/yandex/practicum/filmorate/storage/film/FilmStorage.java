package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Film getById(long id);

    Collection<Film> getAll();

    void delete(long id);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    List<Film> getPopular(int count);
}