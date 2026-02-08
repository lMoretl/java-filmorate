package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private Integer id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;
}
