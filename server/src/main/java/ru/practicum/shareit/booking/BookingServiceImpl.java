package ru.practicum.shareit.booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingDto getById(Long userId, Long id) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с указанным id не найден"));
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Бронирования с таким id не найдено"));
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId))
            throw new ForbiddenException("У вас нет прав для просмотра содержимого");

        return BookingMapper.toBookingDto(booking);
    }

    public Collection<BookingDto> getUserBookings(Long userId, State state) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с указанным id не найден"));

        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> bookings;

        switch (state) {
            case ALL -> bookings =
                    bookingRepository.findAllByBookerIdOrderByStartDesc(userId);

            case CURRENT -> bookings =
                    bookingRepository
                            .findAllByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                                    userId, now, now
                            );

            case PAST -> bookings =
                    bookingRepository
                            .findAllByBookerIdAndEndBeforeOrderByStartDesc(
                                    userId, now
                            );

            case FUTURE -> bookings =
                    bookingRepository
                            .findAllByBookerIdAndStartAfterOrderByStartDesc(
                                    userId, now
                            );

            case WAITING -> bookings =
                    bookingRepository
                            .findAllByBookerIdAndStatusOrderByStartDesc(
                                    userId, Status.WAITING
                            );

            case REJECTED -> bookings =
                    bookingRepository
                            .findAllByBookerIdAndStatusOrderByStartDesc(
                                    userId, Status.REJECTED
                            );

            default -> throw new IllegalArgumentException("Неопознанный статус: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    public Collection<BookingDto> getUserItemsBookings(Long userId, State state) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с указанным id не найден"));

        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> bookings;

        switch (state) {
            case ALL -> bookings =
                    bookingRepository
                            .findAllByItemOwnerIdOrderByStartDesc(userId);

            case CURRENT -> bookings =
                    bookingRepository
                            .findAllByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                                    userId, now, now
                            );

            case PAST -> bookings =
                    bookingRepository
                            .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                                    userId, now
                            );

            case FUTURE -> bookings =
                    bookingRepository
                            .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                                    userId, now
                            );

            case WAITING -> bookings =
                    bookingRepository
                            .findAllByItemOwnerIdAndStatusOrderByStartDesc(
                                    userId, Status.WAITING
                            );

            case REJECTED -> bookings =
                    bookingRepository
                            .findAllByItemOwnerIdAndStatusOrderByStartDesc(
                                    userId, Status.REJECTED
                            );

            default -> throw new IllegalArgumentException("Неопознанный статус: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Transactional
    public BookingDto createBooking(Long userId, NewBookingDto bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с указанным id не найден"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() ->
                        new NotFoundException("Предмет с указанным id не найден"));

        if (item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Владелец не может бронировать собственную вещь");
        }
        if (!item.getAvailable().equals(Boolean.TRUE)) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto changeStatus(Long userId, Long id, boolean approve) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Бронирования с таким id не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId))
            throw new ForbiddenException("У вас нет прав для изменения содержимого");
        if (booking.getStatus() != Status.WAITING)
            throw new ConflictException("Статус бронирования уже был изменён");

        booking.setStatus(approve ? Status.APPROVED : Status.REJECTED);

        return BookingMapper.toBookingDto(booking);
    }
}
