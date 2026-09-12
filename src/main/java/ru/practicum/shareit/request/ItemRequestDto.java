package ru.practicum.shareit.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;
    private String description;
    private Long requestor;
    private Instant created;
}
