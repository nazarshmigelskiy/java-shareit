package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;


@UtilityClass
public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                new BookingItemDto(
                        booking.getItem().getId(),
                        booking.getItem().getName()
                ),
                new BookingUserDto(
                        booking.getBooker().getId(),
                        booking.getBooker().getName()
                ),
                booking.getStatus()
        );
    }
}
