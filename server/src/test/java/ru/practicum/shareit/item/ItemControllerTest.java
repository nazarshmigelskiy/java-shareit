package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingShortDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemService itemService;

    private final ItemDto itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, null,
            new BookingShortDto(5L, 2L), null, List.of());

    private final String itemJson = "{\"name\":\"Drill\",\"description\":\"Powerful drill\",\"available\":true}";

    @Test
    void getAllItems() throws Exception {
        when(itemService.getAll()).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void getByUserId() throws Exception {
        when(itemService.getByUserId(1L)).thenReturn(List.of(itemDto));

        mvc.perform(get("/items").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getById() throws Exception {
        when(itemService.getById(1L, 1L)).thenReturn(itemDto);

        mvc.perform(get("/items/1").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.lastBooking.id").value(5))
                .andExpect(jsonPath("$.lastBooking.bookerId").value(2))
                .andExpect(jsonPath("$.nextBooking").doesNotExist())
                .andExpect(jsonPath("$.comments").isEmpty());
    }

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(eq(1L), any())).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(eq(1L), eq(1L), any())).thenReturn(itemDto);

        mvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void updateItemByNotOwnerReturnsForbidden() throws Exception {
        when(itemService.updateItem(eq(2L), eq(1L), any()))
                .thenThrow(new ForbiddenException("Редактировать страницу вещи может только владелец"));

        mvc.perform(patch("/items/1")
                        .header(USER_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\":true}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.description").value("Редактировать страницу вещи может только владелец"));
    }

    @Test
    void search() throws Exception {
        when(itemService.search("drill")).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void deleteItem() throws Exception {
        mvc.perform(delete("/items/1").header(USER_HEADER, 1))
                .andExpect(status().isOk());

        verify(itemService).deleteItem(1L, 1L);
    }

    @Test
    void addComment() throws Exception {
        CommentDto comment = new CommentDto(1L, "Great drill", "Booker", LocalDateTime.of(2030, 1, 1, 12, 0));
        when(itemService.addComment(eq(2L), eq(1L), any())).thenReturn(comment);

        mvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Great drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great drill"))
                .andExpect(jsonPath("$.authorName").value("Booker"))
                .andExpect(jsonPath("$.created").value("2030-01-01T12:00:00"));
    }

    @Test
    void addCommentWithoutBookingReturnsBadRequest() throws Exception {
        when(itemService.addComment(eq(2L), eq(1L), any()))
                .thenThrow(new BadRequestException("Пользователь не может оставить комментарий"));

        mvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Great drill\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Некорректный запрос"));
    }
}
