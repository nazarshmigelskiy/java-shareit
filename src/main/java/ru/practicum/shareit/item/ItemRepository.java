package ru.practicum.shareit.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Collection;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Getter
public class ItemRepository {
    private final Map<Long, Item> itemList;

    public Collection<Item> getAll() {
        return itemList.values();
    }

    public Item getById(Long id) {
        if (!itemList.containsKey(id)) throw new NotFoundException("Предмета с указанным id не существует");
        return itemList.get(id);
    }

    public Collection<Item> getByUserId(Long userId) {
        return itemList.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .toList();
    }

    public Item createItem(Item item) {
        item.setId(createNewId());
        itemList.put(item.getId(), item);
        return item;
    }

    public Item updateItem(Item item) {
        return itemList.put(item.getId(), item);
    }

    public void deleteItem(Long id) {
        itemList.remove(id);
    }

    private Long createNewId() {
        long currentMaxId = itemList.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        return currentMaxId + 1;
    }
}
