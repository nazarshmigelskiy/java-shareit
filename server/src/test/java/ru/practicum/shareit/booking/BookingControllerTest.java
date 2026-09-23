package ru.practicum.shareit.booking;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private BookingService bookingService;

    private final BookingDto bookingDto = new BookingDto(1L,
            LocalDateTime.of(2030, 1, 1, 12, 0),
            LocalDateTime.of(2030, 1, 2, 12, 0),
            new BookingItemDto(3L, "Drill"),
            new BookingUserDto(2L, "Booker"),
            Status.WAITING);

    @Test
    void getById() throws Exception {
        when(bookingService.getById(2L, 1L)).thenReturn(bookingDto);

        mvc.perform(get("/bookings/1").header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.start").value("2030-01-01T12:00:00"))
                .andExpect(jsonPath("$.item.name").value("Drill"))
                .andExpect(jsonPath("$.booker.id").value(2))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getByIdNotFound() throws Exception {
        when(bookingService.getById(2L, 99L)).thenThrow(new NotFoundException("Бронирования с таким id не найдено"));

        mvc.perform(get("/bookings/99").header(USER_HEADER, 2))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookingsWithDefaultState() throws Exception {
        when(bookingService.getUserBookings(2L, State.ALL)).thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings").header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getUserBookingsWithState() throws Exception {
        when(bookingService.getUserBookings(2L, State.FUTURE)).thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings").header(USER_HEADER, 2).param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getUserItemsBookings() throws Exception {
        when(bookingService.getUserItemsBookings(3L, State.WAITING)).thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner").header(USER_HEADER, 3).param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].item.id").value(3));
    }

    @Test
    void createBooking() throws Exception {
        when(bookingService.createBooking(eq(2L), any())).thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":3,\"start\":\"2030-01-01T12:00:00\",\"end\":\"2030-01-02T12:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.end").value("2030-01-02T12:00:00"));
    }

    @Test
    void changeStatus() throws Exception {
        BookingDto approved = new BookingDto(1L, bookingDto.getStart(), bookingDto.getEnd(),
                bookingDto.getItem(), bookingDto.getBooker(), Status.APPROVED);
        when(bookingService.changeStatus(3L, 1L, true)).thenReturn(approved);

        mvc.perform(patch("/bookings/1").header(USER_HEADER, 3).param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
