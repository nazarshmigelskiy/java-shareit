package ru.practicum.shareit.booking;

import java.util.Collection;

public interface BookingService {

    BookingDto getById(Long userId, Long id);

    Collection<BookingDto> getUserBookings(Long userId, State state);

    Collection<BookingDto> getUserItemsBookings(Long userId, State state);

    BookingDto createBooking(Long id, NewBookingDto bookingDto);

    BookingDto changeStatus(Long userId, Long id, boolean approve);
}
