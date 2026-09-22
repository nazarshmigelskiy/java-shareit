package ru.practicum.shareit.request;

import java.util.Collection;

public interface ItemRequestService {

    Collection<ItemRequestDto> getUserRequests(Long userId);

    Collection<ItemRequestDto> getAllRequests(Long userId);

    ItemRequestDto getRequestById(Long requestId);

    ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto);
}
