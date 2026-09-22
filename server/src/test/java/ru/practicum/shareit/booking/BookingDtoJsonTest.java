package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {
    @Autowired
    private JacksonTester<BookingDto> bookingJson;
    @Autowired
    private JacksonTester<NewBookingDto> newBookingJson;

    @Test
    void serializeBookingDto() throws Exception {
        BookingDto dto = new BookingDto(1L,
                LocalDateTime.of(2030, 1, 1, 12, 0),
                LocalDateTime.of(2030, 1, 2, 12, 30, 15),
                new BookingItemDto(3L, "Drill"),
                new BookingUserDto(2L, "Booker"),
                Status.APPROVED);

        JsonContent<BookingDto> result = bookingJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2030-01-01T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2030-01-02T12:30:15");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Booker");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }

    @Test
    void deserializeNewBookingDto() throws Exception {
        String content = "{\"itemId\":3,\"start\":\"2030-01-01T12:00:00\",\"end\":\"2030-01-02T12:30:15\"}";

        NewBookingDto dto = newBookingJson.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(3L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 1, 1, 12, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2030, 1, 2, 12, 30, 15));
    }

    @Test
    void serializeNewBookingDto() throws Exception {
        NewBookingDto dto = new NewBookingDto(3L,
                LocalDateTime.of(2030, 1, 1, 12, 0),
                LocalDateTime.of(2030, 1, 2, 12, 0));

        JsonContent<NewBookingDto> result = newBookingJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2030-01-01T12:00:00");
    }
}
