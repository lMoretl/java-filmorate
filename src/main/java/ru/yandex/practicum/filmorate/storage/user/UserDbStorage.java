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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private static final String INSERT_USER =
            "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_USER =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    private static final String FIND_USER_BY_ID =
            "SELECT * FROM users WHERE id = ?";

    private static final String FIND_ALL_USERS =
            "SELECT * FROM users ORDER BY id";

    private static final String DELETE_USER =
            "DELETE FROM users WHERE id = ?";

    private static final String CHECK_FRIENDSHIP_EXISTS =
            "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";

    private static final String INSERT_FRIENDSHIP =
            "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";

    private static final String DELETE_FRIENDSHIP =
            "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";

    private static final String FIND_FRIENDS = """
            SELECT u.*
            FROM friendships f
            JOIN users u ON u.id = f.friend_id
            WHERE f.user_id = ?
            ORDER BY u.id
            """;

    private static final String FIND_COMMON_FRIENDS = """
            SELECT u.*
            FROM friendships f1
            JOIN friendships f2 ON f1.friend_id = f2.friend_id
            JOIN users u ON u.id = f1.friend_id
            WHERE f1.user_id = ?
              AND f2.user_id = ?
            ORDER BY u.id
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER, new String[]{"id"});
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

        jdbcTemplate.update(UPDATE_USER,
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
        List<User> users = jdbcTemplate.query(FIND_USER_BY_ID, this::mapUser, id);

        if (users.isEmpty()) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        return users.get(0);
    }

    @Override
    public Collection<User> getAll() {
        return jdbcTemplate.query(FIND_ALL_USERS, this::mapUser);
    }

    @Override
    public void delete(long id) {
        getById(id);
        jdbcTemplate.update(DELETE_USER, id);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        Integer friendshipExists = jdbcTemplate.queryForObject(
                CHECK_FRIENDSHIP_EXISTS,
                Integer.class,
                userId, friendId
        );

        if (friendshipExists != null && friendshipExists > 0) {
            return;
        }

        jdbcTemplate.update(INSERT_FRIENDSHIP, userId, friendId, "UNCONFIRMED");
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        jdbcTemplate.update(DELETE_FRIENDSHIP, userId, friendId);
    }

    @Override
    public List<User> getFriends(long userId) {
        return jdbcTemplate.query(FIND_FRIENDS, this::mapUser, userId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        return jdbcTemplate.query(FIND_COMMON_FRIENDS, this::mapUser, userId, otherUserId);
    }

    private User mapUser(ResultSet rs, int rowNum) throws SQLException {
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