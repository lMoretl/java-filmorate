package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.NoSuchElementException;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM mpa ORDER BY id",
                (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("name"))
        );
    }

    public Mpa getById(int id) {
        List<Mpa> mpaList = jdbcTemplate.query(
                "SELECT * FROM mpa WHERE id = ?",
                (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("name")),
                id
        );

        if (mpaList.isEmpty()) {
            throw new NoSuchElementException("Рейтинг MPA не найден");
        }

        return mpaList.get(0);
    }
}