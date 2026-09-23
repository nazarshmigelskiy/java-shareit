package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {
    @Autowired
    private JacksonTester<ItemDto> itemJson;
    @Autowired
    private JacksonTester<CommentDto> commentJson;

    @Test
    void serializeItemDtoWithBookingsAndComments() throws Exception {
        CommentDto comment = new CommentDto(3L, "Great drill", "Booker", LocalDateTime.of(2030, 1, 1, 12, 0));
        ItemDto dto = new ItemDto(1L, "Drill", "Powerful drill", true, 7L,
                new BookingShortDto(5L, 2L), new BookingShortDto(6L, 2L), List.of(comment));

        JsonContent<ItemDto> result = itemJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(7);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(5);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(6);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("Booker");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created").isEqualTo("2030-01-01T12:00:00");
    }

    @Test
    void deserializeItemDtoIgnoresId() throws Exception {
        String content = "{\"id\":42,\"name\":\"Drill\",\"description\":\"Powerful drill\","
                + "\"available\":false,\"requestId\":7}";

        ItemDto dto = itemJson.parseObject(content);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getRequestId()).isEqualTo(7L);
    }

    @Test
    void deserializeCommentDto() throws Exception {
        CommentDto dto = commentJson.parseObject("{\"text\":\"Great drill\"}");

        assertThat(dto.getText()).isEqualTo("Great drill");
        assertThat(dto.getAuthorName()).isNull();
    }
}
