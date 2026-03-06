package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreDbStorage genreStorage;

    @GetMapping
    public List<Genre> getAll() {
        return genreStorage.getAll();
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable int id) {
        return genreStorage.getById(id);
    }
}