package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{id}")
    public BookingDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long id) {
        return bookingService.getById(userId, id);
    }

    @GetMapping({"", "/"})
    public Collection<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "ALL") State state) {
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getUserItemsBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(defaultValue = "ALL") State state) {
        return bookingService.getUserItemsBookings(userId, state);
    }

    @PostMapping({"", "/"})
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestBody NewBookingDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{id}")
    public BookingDto changeStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @PathVariable Long id,
                                   @RequestParam Boolean approved) {
        return bookingService.changeStatus(userId, id, approved);

    }
}
