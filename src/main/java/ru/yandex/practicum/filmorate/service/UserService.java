package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public List<User> getAll() {
        return List.copyOf(userStorage.getAll());
    }

    public User getById(long id) {
        return userStorage.getById(id);
    }

    public User create(User user) {
        validateLogin(user);
        normalizeName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        validateLogin(user);
        normalizeName(user);
        return userStorage.update(user);
    }

    public void addFriend(long id, long friendId) {
        if (id == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        userStorage.getById(id);
        userStorage.getById(friendId);

        Integer reverseExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                friendId, id
        );

        String status = (reverseExists != null && reverseExists > 0)
                ? "CONFIRMED"
                : "UNCONFIRMED";

        jdbcTemplate.update(
                "MERGE INTO friendships (user_id, friend_id, status) KEY (user_id, friend_id) VALUES (?, ?, ?)",
                id, friendId, status
        );

        if ("CONFIRMED".equals(status)) {
            jdbcTemplate.update(
                    "UPDATE friendships SET status = 'CONFIRMED' WHERE user_id = ? AND friend_id = ?",
                    friendId, id
            );
        }
    }

    public void removeFriend(long id, long friendId) {
        userStorage.getById(id);
        userStorage.getById(friendId);

        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                id, friendId
        );

        jdbcTemplate.update(
                "UPDATE friendships SET status = 'UNCONFIRMED' WHERE user_id = ? AND friend_id = ?",
                friendId, id
        );
    }

    public List<User> getFriends(long id) {
        userStorage.getById(id);

        return jdbcTemplate.query(
                """
                SELECT u.*
                FROM friendships f
                JOIN users u ON u.id = f.friend_id
                WHERE f.user_id = ?
                ORDER BY u.id
                """,
                (rs, rowNum) -> mapUser(rs),
                id
        );
    }

    public List<User> getCommonFriends(long id, long otherId) {
        userStorage.getById(id);
        userStorage.getById(otherId);

        return jdbcTemplate.query(
                """
                SELECT u.*
                FROM friendships f1
                JOIN friendships f2 ON f1.friend_id = f2.friend_id
                JOIN users u ON u.id = f1.friend_id
                WHERE f1.user_id = ?
                  AND f2.user_id = ?
                ORDER BY u.id
                """,
                (rs, rowNum) -> mapUser(rs),
                id, otherId
        );
    }

    private void validateLogin(User user) {
        if (user.getLogin() != null && user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private User mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));

        Date birthday = rs.getDate("birthday");
        user.setBirthday(birthday == null ? null : birthday.toLocalDate());

        return user;
    }
}