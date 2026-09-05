package ru.practicum.shareit.item;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Collection<Item> getAll() {
        return itemRepository.getAll();
    }

    public Item getById(Long id) {
        return itemRepository.getById(id);
    }

    public Collection<Item> getByUserId(Long userId) {
        return itemRepository.getByUserId(userId);
    }

    public Item createItem(Long userId, Item item) {
        if (!userRepository.getUserList().containsKey(userId)) {
            throw new NotFoundException("Пользователя с таким id не существует");
        }
        item.setOwner(userId);
        return itemRepository.createItem(item);
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.getById(itemId);

        if (!item.getOwner().equals(userId))
            throw new ForbiddenException("Редактировать страницу вещи может только владелец");

        if (itemDto.getName() != null) item.setName(itemDto.getName());

        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());

        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        return ItemMapper.toItemDto(itemRepository.updateItem(item));
    }

    public void deleteItem(Long userId, Long itemId) {
        Item item = itemRepository.getById(itemId);

        if (!item.getOwner().equals(userId))
            throw new ForbiddenException("Удалить страницу вещи может только владелец");

        itemRepository.deleteItem(itemId);
    }

    public Collection<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String lowerText = text.toLowerCase();
        return getAll().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable()
                        && (item.getName().toLowerCase().contains(lowerText)
                        || item.getDescription().toLowerCase().contains(lowerText)))
                .toList();
    }
}
