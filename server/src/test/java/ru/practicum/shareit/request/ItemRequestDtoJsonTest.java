package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serializeItemRequestDtoWithItems() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Need a drill", 2L,
                LocalDateTime.of(2030, 1, 1, 12, 0, 30), List.of(new ResponseDto(5L, "Drill", 3L)));

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(result).extractingJsonPathNumberValue("$.requestor").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2030-01-01T12:00:30");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].itemId").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(3);
    }

    @Test
    void deserializeItemRequestDto() throws Exception {
        ItemRequestDto dto = json.parseObject("{\"description\":\"Need a drill\"}");

        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getId()).isNull();
        assertThat(dto.getItems()).isNull();
    }
}
