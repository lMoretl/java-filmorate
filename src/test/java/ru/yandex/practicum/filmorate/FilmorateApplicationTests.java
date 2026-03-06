package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class})
class FilmorateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @Test
    void testCreateAndFindUserById() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userStorage.create(user);

        assertNotNull(created.getId());

        User found = userStorage.getById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("test@mail.ru", found.getEmail());
        assertEquals("testLogin", found.getLogin());
        assertEquals("Test User", found.getName());
        assertEquals(LocalDate.of(2000, 1, 1), found.getBirthday());
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("old@mail.ru");
        user.setLogin("oldLogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1999, 5, 5));

        User created = userStorage.create(user);

        created.setEmail("new@mail.ru");
        created.setLogin("newLogin");
        created.setName("New Name");

        User updated = userStorage.update(created);

        assertEquals(created.getId(), updated.getId());
        assertEquals("new@mail.ru", updated.getEmail());
        assertEquals("newLogin", updated.getLogin());
        assertEquals("New Name", updated.getName());
        assertEquals(LocalDate.of(1999, 5, 5), updated.getBirthday());
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setEmail("u1@mail.ru");
        user1.setLogin("u1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(2001, 1, 1));
        userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("u2@mail.ru");
        user2.setLogin("u2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2002, 2, 2));
        userStorage.create(user2);

        Collection<User> users = userStorage.getAll();

        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }

    @Test
    void testCreateAndFindFilmById() {
        Film film = new Film();
        film.setName("Interstellar");
        film.setDescription("Sci-fi movie");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);
        film.setMpa(new Mpa(3, "PG-13"));
        film.setGenres(Set.of(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        ));

        Film created = filmStorage.create(film);

        assertNotNull(created.getId());

        Film found = filmStorage.getById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Interstellar", found.getName());
        assertEquals("Sci-fi movie", found.getDescription());
        assertEquals(LocalDate.of(2014, 11, 7), found.getReleaseDate());
        assertEquals(169, found.getDuration());

        assertNotNull(found.getMpa());
        assertEquals(3, found.getMpa().getId());
        assertEquals("PG-13", found.getMpa().getName());

        assertNotNull(found.getGenres());
        assertEquals(2, found.getGenres().size());
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Old Film");
        film.setDescription("Old description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));
        film.setGenres(Set.of(new Genre(1, "Комедия")));

        Film created = filmStorage.create(film);

        created.setName("New Film");
        created.setDescription("New description");
        created.setDuration(150);
        created.setMpa(new Mpa(4, "R"));
        created.setGenres(Set.of(
                new Genre(2, "Драма"),
                new Genre(4, "Триллер")
        ));

        Film updated = filmStorage.update(created);

        assertEquals("New Film", updated.getName());
        assertEquals("New description", updated.getDescription());
        assertEquals(150, updated.getDuration());

        assertNotNull(updated.getMpa());
        assertEquals(4, updated.getMpa().getId());
        assertEquals("R", updated.getMpa().getName());

        assertNotNull(updated.getGenres());
        assertEquals(2, updated.getGenres().size());
    }

    @Test
    void testGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Desc 1");
        film1.setReleaseDate(LocalDate.of(2001, 1, 1));
        film1.setDuration(100);
        film1.setMpa(new Mpa(1, "G"));
        film1.setGenres(Set.of(new Genre(1, "Комедия")));
        filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Desc 2");
        film2.setReleaseDate(LocalDate.of(2002, 2, 2));
        film2.setDuration(110);
        film2.setMpa(new Mpa(2, "PG"));
        film2.setGenres(Set.of(new Genre(2, "Драма")));
        filmStorage.create(film2);

        Collection<Film> films = filmStorage.getAll();

        assertNotNull(films);
        assertTrue(films.size() >= 2);
    }
}