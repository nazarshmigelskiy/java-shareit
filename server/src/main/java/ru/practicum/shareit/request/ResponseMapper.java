package ru.practicum.shareit.request;

import ru.practicum.shareit.item.Item;

public class ResponseMapper {

    public static ResponseDto toResponseDto(Item item) {
        return new ResponseDto(
                item.getId(),
                item.getName(),
                item.getOwner().getId()
        );
    }
}
