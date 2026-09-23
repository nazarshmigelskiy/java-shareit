package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserService userService;

    private final UserDto userDto = new UserDto(1L, "Ivan", "ivan@mail.ru");

    @Test
    void getUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Ivan"));
    }

    @Test
    void getById() throws Exception {
        when(userService.getById(1L)).thenReturn(userDto);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));
    }

    @Test
    void getByIdNotFound() throws Exception {
        when(userService.getById(99L)).thenThrow(new NotFoundException("Пользователь с таким id не найден"));

        mvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Объект не найден"))
                .andExpect(jsonPath("$.description").value("Пользователь с таким id не найден"));
    }

    @Test
    void createUser() throws Exception {
        when(userService.createUser(any())).thenReturn(userDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));
    }

    @Test
    void createUserWithDuplicateEmailReturnsConflict() throws Exception {
        when(userService.createUser(any())).thenThrow(new ConflictException("Пользователь с указанным Email уже существует"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.description").value("Пользователь с указанным Email уже существует"));
    }

    @Test
    void updateUser() throws Exception {
        UserDto updated = new UserDto(1L, "Petr", "ivan@mail.ru");
        when(userService.updateUser(eq(1L), any())).thenReturn(updated);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Petr\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Petr"));
    }

    @Test
    void deleteUser() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUserUnexpectedErrorReturnsInternalServerError() throws Exception {
        doThrow(new RuntimeException("Сбой")).when(userService).deleteUser(1L);

        mvc.perform(delete("/users/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Непредвиденная ошибка"));
    }
}
