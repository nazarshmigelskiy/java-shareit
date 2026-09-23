package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemRequestService itemRequestService;

    private final ItemRequestDto requestDto = new ItemRequestDto(1L, "Need a drill", 2L,
            LocalDateTime.of(2030, 1, 1, 12, 0), List.of(new ResponseDto(5L, "Drill", 3L)));

    @Test
    void getAllRequests() throws Exception {
        when(itemRequestService.getAllRequests(3L)).thenReturn(List.of(requestDto));

        mvc.perform(get("/requests/all").header(USER_HEADER, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Need a drill"));
    }

    @Test
    void getUserRequests() throws Exception {
        when(itemRequestService.getUserRequests(2L)).thenReturn(List.of(requestDto));

        mvc.perform(get("/requests").header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].items[0].name").value("Drill"));
    }

    @Test
    void getRequestById() throws Exception {
        when(itemRequestService.getRequestById(1L)).thenReturn(requestDto);

        mvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestor").value(2))
                .andExpect(jsonPath("$.created").value("2030-01-01T12:00:00"))
                .andExpect(jsonPath("$.items[0].itemId").value(5))
                .andExpect(jsonPath("$.items[0].ownerId").value(3));
    }

    @Test
    void getRequestByIdNotFound() throws Exception {
        when(itemRequestService.getRequestById(99L)).thenThrow(new NotFoundException("Запрос с таким id не найден"));

        mvc.perform(get("/requests/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.description").value("Запрос с таким id не найден"));
    }

    @Test
    void createRequest() throws Exception {
        ItemRequestDto created = new ItemRequestDto(1L, "Need a drill", 2L,
                LocalDateTime.of(2030, 1, 1, 12, 0), List.of());
        when(itemRequestService.createRequest(eq(2L), any())).thenReturn(created);

        mvc.perform(post("/requests")
                        .header(USER_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.items").isEmpty());
    }
}
