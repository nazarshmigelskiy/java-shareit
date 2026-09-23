package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getRequestor().getId(),
                request.getCreated(),
                null
        );
    }

    public static ItemRequest toItemRequest(ItemRequestDto requestDto) {
        return new ItemRequest(
                requestDto.getId(),
                requestDto.getDescription(),
                null,
                requestDto.getCreated()
        );
    }
}
