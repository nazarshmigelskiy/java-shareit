package ru.practicum.shareit.item;

import java.util.Collection;

public interface ItemService {
    Collection<Item> getAll();

    Collection<Item> getByUserId(Long userid);

    Item getById(Long id);

    Item createItem(Long userId, Item item);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    void deleteItem(Long userId, Long itemId);

    Collection<Item> search(String text);
}
