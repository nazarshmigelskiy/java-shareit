package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public Collection<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto getById(Long id) {
        return UserMapper.toUserDto(userRepository.getById(id));
    }

    public UserDto createUser(UserDto userDto) {
        if (!validateEmail(userDto.getEmail()))
            throw new ConflictException("Пользователь с указанным Email уже существует");
        return UserMapper.toUserDto(userRepository.createUser(UserMapper.toUser(userDto)));
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = UserMapper.toUser(getById(id));

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (!validateEmailForUpdate(userDto.getEmail(), id))
                throw new ConflictException("Пользователь с указанным Email уже существует");
            user.setEmail(userDto.getEmail());
        }

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

    private boolean validateEmailForUpdate(String email, Long userId) {
        return userRepository.getUserList().values().stream()
                .filter(u -> !u.getId().equals(userId))
                .map(User::getEmail)
                .noneMatch(email::equals);
    }

}
