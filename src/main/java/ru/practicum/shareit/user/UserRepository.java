package ru.practicum.shareit.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Collection;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Getter
public class UserRepository {
    private final Map<Long, User> userList;

    public Collection<User> getAllUsers() {
        return userList.values();
    }

    public User getById(Long id) {
        if (!userList.containsKey(id))
            throw new NotFoundException("Пользователя с указанным id не существует");
        return userList.get(id);
    }

    public User createUser(User user) {
        user.setId(createNewId());
        userList.put(user.getId(), user);
        return user;
    }

    public User updateUser(User user) {
        userList.put(user.getId(), user);
        return user;
    }

    public void deleteUser(Long id) {
        if (!userList.containsKey(id)) throw new NotFoundException("Пользователя с указанным id не существует");
        userList.remove((id));
    }


    private Long createNewId() {
        long currentMaxId = userList.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        return currentMaxId + 1;
    }
}
