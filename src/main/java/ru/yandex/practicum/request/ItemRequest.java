package ru.yandex.practicum.request;

import lombok.Data;
import ru.yandex.practicum.user.User;

import java.time.Instant;

@Data
public class ItemRequest {
    private Long id;
    private String description;
    private User requestor;
    private Instant created;
}
