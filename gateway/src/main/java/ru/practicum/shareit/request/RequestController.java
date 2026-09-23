package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.getAllRequests(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.getUserRequests(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long id) {
        return requestClient.getRequestById(id);
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody RequestDto itemRequestDto) {
        return requestClient.createRequest(userId, itemRequestDto);
    }

}
