package ru.yandex.practicum.user;

import java.util.Collection;

public interface UserService {

    Collection<User> getAllUsers();

    User getById(Long id);

    User createUser(User user);

    UserDto updateUser(Long id, UserDto user);

    void deleteUser(Long id);

}
