package ru.practicum.shareit.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public Collection<ItemRequestDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("Пользователь с таким id не найден");

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);

        return mapRequestsWithResponses(requests);
    }


    public Collection<ItemRequestDto> getAllRequests(Long userId) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("Пользователь с таким id не найден");

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);

        return mapRequestsWithResponses(requests);

    }

    public ItemRequestDto getRequestById(Long requestId) {
        ItemRequest request = itemRequestRepository.findById(requestId).orElseThrow(()
                        -> new NotFoundException("Запрос с таким id не найден"));
        return mapRequestsWithResponses(List.of(request)).getFirst();
    }

    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        User owner = userRepository.findById(userId).orElseThrow(()
                ->  new NotFoundException("Пользователь с таким id не найден"));

        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto);

        request.setRequestor(owner);
        request.setCreated(LocalDateTime.now());

        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(request));
    }

    private List<ItemRequestDto> mapRequestsWithResponses(List<ItemRequest> requests) {
        if (requests.isEmpty()) return List.of();

        List<Long> ids = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Collection<Item> responses = itemRepository.findAllByRequestIdIn(ids);


        Map<Long, List<ResponseDto>> itemsByRequestId = responses.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(
                                ResponseMapper::toResponseDto,
                                Collectors.toList()
                        )
                ));

        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
                    dto.setItems(
                            itemsByRequestId.getOrDefault(
                                    request.getId(),
                                    Collections.emptyList()
                            )
                    );
                    return dto;
                })
                .toList();
    }
}
