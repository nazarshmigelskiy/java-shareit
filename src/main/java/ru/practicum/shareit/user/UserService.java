package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserService {

    Collection<UserDto> getAllUsers();

    UserDto getById(Long id);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long id, UserDto user);

    void deleteUser(Long id);

}
