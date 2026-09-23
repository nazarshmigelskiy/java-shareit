package ru.practicum.shareit.item;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemDto {
    private String name;
    private String description;
    private Boolean available;
}
