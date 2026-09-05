package ru.yandex.practicum.booking;

import lombok.Data;
import ru.yandex.practicum.item.Item;
import ru.yandex.practicum.user.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private User booker;
    private Status status;
}
