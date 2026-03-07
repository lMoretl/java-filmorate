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
import java.util.List;
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
        User user = createUser("test@mail.ru", "testLogin", "Test User", LocalDate.of(2000, 1, 1));

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
        User user = createUser("old@mail.ru", "oldLogin", "Old Name", LocalDate.of(1999, 5, 5));
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
        userStorage.create(createUser("u1@mail.ru", "u1", "User 1", LocalDate.of(2001, 1, 1)));
        userStorage.create(createUser("u2@mail.ru", "u2", "User 2", LocalDate.of(2002, 2, 2)));

        Collection<User> users = userStorage.getAll();

        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }

    @Test
    void testAddAndGetFriends() {
        User user1 = userStorage.create(createUser("user1@mail.ru", "user1", "User 1", LocalDate.of(2000, 1, 1)));
        User user2 = userStorage.create(createUser("user2@mail.ru", "user2", "User 2", LocalDate.of(2000, 2, 2)));

        userStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());

        assertEquals(1, friends.size());
        assertEquals(user2.getId(), friends.get(0).getId());
    }

    @Test
    void testRemoveFriend() {
        User user1 = userStorage.create(createUser("user3@mail.ru", "user3", "User 3", LocalDate.of(2000, 3, 3)));
        User user2 = userStorage.create(createUser("user4@mail.ru", "user4", "User 4", LocalDate.of(2000, 4, 4)));

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    void testGetCommonFriends() {
        User user1 = userStorage.create(createUser("common1@mail.ru", "common1", "Common 1", LocalDate.of(2000, 1, 1)));
        User user2 = userStorage.create(createUser("common2@mail.ru", "common2", "Common 2", LocalDate.of(2000, 2, 2)));
        User commonFriend = userStorage.create(createUser("common3@mail.ru", "common3", "Common 3", LocalDate.of(2000, 3, 3)));

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.get(0).getId());
    }

    @Test
    void testCreateAndFindFilmById() {
        Film film = createFilm(
                "Interstellar",
                "Sci-fi movie",
                LocalDate.of(2014, 11, 7),
                169,
                new Mpa(3, "PG-13"),
                Set.of(
                        new Genre(1, "Комедия"),
                        new Genre(2, "Драма")
                )
        );

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
        Film film = createFilm(
                "Old Film",
                "Old description",
                LocalDate.of(2000, 1, 1),
                120,
                new Mpa(1, "G"),
                Set.of(new Genre(1, "Комедия"))
        );

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
        filmStorage.create(createFilm(
                "Film 1",
                "Desc 1",
                LocalDate.of(2001, 1, 1),
                100,
                new Mpa(1, "G"),
                Set.of(new Genre(1, "Комедия"))
        ));

        filmStorage.create(createFilm(
                "Film 2",
                "Desc 2",
                LocalDate.of(2002, 2, 2),
                110,
                new Mpa(2, "PG"),
                Set.of(new Genre(2, "Драма"))
        ));

        Collection<Film> films = filmStorage.getAll();

        assertNotNull(films);
        assertTrue(films.size() >= 2);
    }

    @Test
    void testAddAndRemoveLike() {
        User user = userStorage.create(createUser("like@mail.ru", "likeUser", "Like User", LocalDate.of(2001, 1, 1)));

        Film film = filmStorage.create(createFilm(
                "Liked Film",
                "Liked description",
                LocalDate.of(2010, 1, 1),
                100,
                new Mpa(1, "G"),
                Set.of(new Genre(1, "Комедия"))
        ));

        filmStorage.addLike(film.getId(), user.getId());
        Film filmWithLike = filmStorage.getById(film.getId());

        assertEquals(1, filmWithLike.getLikes().size());
        assertTrue(filmWithLike.getLikes().contains(user.getId()));

        filmStorage.removeLike(film.getId(), user.getId());
        Film filmWithoutLike = filmStorage.getById(film.getId());

        assertTrue(filmWithoutLike.getLikes().isEmpty());
    }

    @Test
    void testGetPopularFilms() {
        User user1 = userStorage.create(createUser("pop1@mail.ru", "pop1", "Pop 1", LocalDate.of(2001, 1, 1)));
        User user2 = userStorage.create(createUser("pop2@mail.ru", "pop2", "Pop 2", LocalDate.of(2002, 2, 2)));

        Film film1 = filmStorage.create(createFilm(
                "Popular Film",
                "Popular description",
                LocalDate.of(2010, 1, 1),
                100,
                new Mpa(1, "G"),
                Set.of(new Genre(1, "Комедия"))
        ));

        Film film2 = filmStorage.create(createFilm(
                "Less Popular Film",
                "Less popular description",
                LocalDate.of(2011, 1, 1),
                110,
                new Mpa(2, "PG"),
                Set.of(new Genre(2, "Драма"))
        ));

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user1.getId());

        List<Film> popularFilms = filmStorage.getPopular(10);

        assertFalse(popularFilms.isEmpty());
        assertEquals(film1.getId(), popularFilms.get(0).getId());
        assertEquals(film2.getId(), popularFilms.get(1).getId());
    }

    private User createUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    private Film createFilm(String name,
                            String description,
                            LocalDate releaseDate,
                            Integer duration,
                            Mpa mpa,
                            Set<Genre> genres) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        film.setMpa(mpa);
        film.setGenres(genres);
        return film;
    }
}