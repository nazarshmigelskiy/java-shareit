package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {
    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void serializeIncludesId() throws Exception {
        JsonContent<UserDto> result = json.write(new UserDto(1L, "Ivan", "ivan@mail.ru"));

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Ivan");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan@mail.ru");
    }

    @Test
    void deserializeIgnoresId() throws Exception {
        UserDto dto = json.parseObject("{\"id\":42,\"name\":\"Ivan\",\"email\":\"ivan@mail.ru\"}");

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isEqualTo("Ivan");
        assertThat(dto.getEmail()).isEqualTo("ivan@mail.ru");
    }
}
