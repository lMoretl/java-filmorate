package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
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
        User user = userStorage.getById(id);
        User friend = userStorage.getById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public void removeFriend(long id, long friendId) {
        User user = userStorage.getById(id);
        User friend = userStorage.getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public List<User> getFriends(long id) {
        User user = userStorage.getById(id);

        return user.getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long id, long otherId) {
        User user = userStorage.getById(id);
        User otherUser = userStorage.getById(otherId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    private void validateLogin(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
