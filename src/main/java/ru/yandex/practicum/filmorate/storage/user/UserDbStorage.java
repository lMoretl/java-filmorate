package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User user) {
        getById(user.getId());

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()),
                user.getId()
        );

        return getById(user.getId());
    }

    @Override
    public User getById(long id) {
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM users WHERE id = ?",
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setEmail(rs.getString("email"));
                    user.setLogin(rs.getString("login"));
                    user.setName(rs.getString("name"));
                    Date birthday = rs.getDate("birthday");
                    user.setBirthday(birthday == null ? null : birthday.toLocalDate());
                    return user;
                },
                id
        );

        if (users.isEmpty()) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        return users.get(0);
    }

    @Override
    public Collection<User> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM users ORDER BY id",
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setEmail(rs.getString("email"));
                    user.setLogin(rs.getString("login"));
                    user.setName(rs.getString("name"));
                    Date birthday = rs.getDate("birthday");
                    user.setBirthday(birthday == null ? null : birthday.toLocalDate());
                    return user;
                }
        );
    }

    @Override
    public void delete(long id) {
        getById(id);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }
}