package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

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
        userStorage.addFriend(id, friendId);
    }

    public void removeFriend(long id, long friendId) {
        userStorage.getById(id);
        userStorage.getById(friendId);
        userStorage.removeFriend(id, friendId);
    }

    public List<User> getFriends(long id) {
        userStorage.getById(id);
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(long id, long otherId) {
        userStorage.getById(id);
        userStorage.getById(otherId);
        return userStorage.getCommonFriends(id, otherId);
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
}