package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping
    public Collection<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/{id}")
    public  ItemRequestDto getRequestById(@PathVariable Long id) {
        return itemRequestService.getRequestById(id);
    }

    @PostMapping
    public  ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.createRequest(userId, itemRequestDto);
    }

}
