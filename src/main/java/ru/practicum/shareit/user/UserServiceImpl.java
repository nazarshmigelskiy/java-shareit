package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public Collection<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public User getById(Long id) {
        return userRepository.getById(id);
    }

    public User createUser(User user) {
        if (!validateEmail(user.getEmail()))
            throw new ConflictException("Пользователь с указанным Email уже существует");
        return userRepository.createUser(user);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = getById(id);

        if (!validateEmail(userDto.getEmail()))
            throw new ConflictException("Пользователь с указанным Email уже существует");

        if (userDto.getName() != null && !userDto.getName().isBlank()) user.setName(userDto.getName());

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) user.setEmail(userDto.getEmail());

        return UserMapper.toUserDto(userRepository.updateUser(user));
    }

    public void deleteUser(Long id) {
        userRepository.deleteUser(id);
    }

    private boolean validateEmail(String email) {
        return userRepository.getUserList().values().stream()
                .map(User::getEmail)
                .noneMatch(email1 -> email1.equals(email));
    }

}
