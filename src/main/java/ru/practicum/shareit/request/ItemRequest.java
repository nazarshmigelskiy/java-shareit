package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.Instant;

@Data
public class ItemRequest {
    private Long id;
    private String description;
    private User requestor;
    private Instant created;
}
