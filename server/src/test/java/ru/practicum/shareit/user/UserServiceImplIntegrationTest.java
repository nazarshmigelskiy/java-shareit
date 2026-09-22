package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserServiceImplIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void createUserAndGetById() {
        UserDto created = userService.createUser(new UserDto(null, "Ivan", "ivan@mail.ru"));

        UserDto found = userService.getById(created.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Ivan");
        assertThat(found.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void createUserWithDuplicateEmailThrowsConflict() {
        userService.createUser(new UserDto(null, "Ivan", "ivan@mail.ru"));

        assertThrows(ConflictException.class,
                () -> userService.createUser(new UserDto(null, "Petr", "ivan@mail.ru")));
    }

    @Test
    void getByIdUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getById(999_999L));
    }

    @Test
    void getAllUsersReturnsAllCreated() {
        userService.createUser(new UserDto(null, "Ivan", "ivan@mail.ru"));
        userService.createUser(new UserDto(null, "Petr", "petr@mail.ru"));

        Collection<UserDto> users = userService.getAllUsers();

        assertThat(users).extracting(UserDto::getEmail)
                .contains("ivan@mail.ru", "petr@mail.ru");
    }

    @Test
    void updateUserChangesNameAndEmail() {
        UserDto created = userService.createUser(new UserDto(null, "Ivan", "ivan@mail.ru"));

        UserDto updated = userService.updateUser(created.getId(), new UserDto(null, "Petr", "petr@mail.ru"));

        assertThat(updated.getName()).isEqualTo("Petr");
        assertThat(updated.getEmail()).isEqualTo("petr@mail.ru");
        UserDto found = userService.getById(created.getId());
        assertThat(found.getName()).isEqualTo("Petr");
        assertThat(found.getEmail()).isEqualTo("petr@mail.ru");
    }

    @Test
    void updateUserWithOnlyNameKeepsEmail() {
        UserDto created = userService.createUser(new UserDto(null, "Ivan", "ivan@mail.ru"));

        userService.updateUser(created.getId(), new UserDto(null, "Petr", null));

        UserDto found = userService.getById(created.getId());
        assertThat(found.getName()).isEqualTo("Petr");
        assertThat(found.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void updateUserWithBlankFieldsChangesNothing() {
        UserDto created = userService.createUser(new UserDto(null, "Ivan", "ivan@mail.ru"));

        userService.updateUser(created.getId(), new UserDto(null, " ", " "));

        UserDto found = userService.getById(created.getId());
        assertThat(found.getName()).isEqualTo("Ivan");
        assertThat(found.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void updateUserWithEmailOfAnotherUserThrowsConflict() {
        userService.createUser(new UserDto(null, "Ivan", "ivan@mail.ru"));
        UserDto petr = userService.createUser(new UserDto(null, "Petr", "petr@mail.ru"));

        assertThrows(ConflictException.class,
                () -> userService.updateUser(petr.getId(), new UserDto(null, null, "ivan@mail.ru")));
    }

    @Test
    void updateUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.updateUser(999_999L, new UserDto(null, "Petr", null)));
    }

    @Test
    void deleteUserRemovesUser() {
        UserDto created = userService.createUser(new UserDto(null, "Ivan", "ivan@mail.ru"));

        userService.deleteUser(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
    }

    @Test
    void deleteUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> userService.deleteUser(999_999L));
    }
}
