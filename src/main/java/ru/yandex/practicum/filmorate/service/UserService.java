package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAll() {
        return List.copyOf(userStorage.getAll());
    }

    public User getById(long id) {
        return userStorage.getById(id);
    }

    public User create(User user) {
        validateUserExtra(user);
        normalizeUser(user);
        User created = userStorage.create(user);
        log.info("Создан пользователь id={}, login={}", created.getId(), created.getLogin());
        return created;
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        validateUserExtra(user);
        normalizeUser(user);
        User updated = userStorage.update(user);
        log.info("Обновлён пользователь id={}, login={}", updated.getId(), updated.getLogin());
        return updated;
    }

    public void addFriend(long id, long friendId) {
        User user = getExistingUser(id);
        User friend = getExistingUser(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Добавлены друзья: {} <-> {}", id, friendId);
    }

    public void removeFriend(long id, long friendId) {
        User user = getExistingUser(id);
        User friend = getExistingUser(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Удалены из друзей: {} x {}", id, friendId);
    }

    public List<User> getFriends(long id) {
        User user = getExistingUser(id);
        return user.getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long id, long otherId) {
        User user = getExistingUser(id);
        User other = getExistingUser(otherId);

        Set<Long> a = user.getFriends();
        Set<Long> b = other.getFriends();

        return a.stream()
                .filter(b::contains)
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    private User getExistingUser(long id) {
        try {
            return userStorage.getById(id);
        } catch (NoSuchElementException e) {
            throw e;
        }
    }

    private void validateUserExtra(User user) {
        if (user.getLogin() != null && user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
    }

    private void normalizeUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
