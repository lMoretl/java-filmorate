package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, List<Long>> friendships = new HashMap<>();
    private long nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        friendships.put(user.getId(), new ArrayList<>());
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (!users.containsKey(id)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        users.put(id, user);
        return user;
    }

    @Override
    public User getById(long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        return user;
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void delete(long id) {
        if (!users.containsKey(id)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        users.remove(id);
        friendships.remove(id);
        friendships.values().forEach(friendIds -> friendIds.remove(id));
    }

    @Override
    public void addFriend(long userId, long friendId) {
        getById(userId);
        getById(friendId);

        List<Long> friendIds = friendships.get(userId);
        if (!friendIds.contains(friendId)) {
            friendIds.add(friendId);
        }
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        getById(userId);
        getById(friendId);

        friendships.get(userId).remove(friendId);
    }

    @Override
    public List<User> getFriends(long userId) {
        getById(userId);

        return friendships.get(userId).stream()
                .map(this::getById)
                .toList();
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        getById(userId);
        getById(otherUserId);

        List<Long> userFriends = friendships.get(userId);
        List<Long> otherUserFriends = friendships.get(otherUserId);

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(this::getById)
                .toList();
    }
}