package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
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
    }
}
