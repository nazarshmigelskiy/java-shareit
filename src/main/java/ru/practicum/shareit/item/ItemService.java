package ru.practicum.shareit.item;

import java.util.Collection;

public interface ItemService {
    Collection<ItemDto> getAll();

    Collection<ItemDto> getByUserId(Long userid);

    ItemDto getById(Long id);

    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    void deleteItem(Long userId, Long itemId);

    Collection<ItemDto> search(String text);
}
