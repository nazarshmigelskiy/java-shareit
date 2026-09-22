package ru.practicum.shareit.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public Collection<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto getById(Long id) {
        return UserMapper.toUserDto(userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с таким id не найден")));
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail()))
            throw new ConflictException("Пользователь с указанным Email уже существует");

        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = UserMapper.toUser(getById(id));

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id))
                throw new ConflictException("Пользователь с указанным Email уже существует");
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) user.setName(userDto.getName());

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) user.setEmail(userDto.getEmail());

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с таким id не найден"));
        userRepository.delete(user);
    }


}
