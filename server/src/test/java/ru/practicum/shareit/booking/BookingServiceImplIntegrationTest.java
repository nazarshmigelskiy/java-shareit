package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BookingServiceImplIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private UserDto stranger;
    private ItemDto item;

    private BookingDto pastBooking;
    private BookingDto currentBooking;
    private BookingDto futureBooking;
    private BookingDto rejectedBooking;

    @BeforeEach
    void setUp() {
        owner = userService.createUser(new UserDto(null, "Owner", "owner@mail.ru"));
        booker = userService.createUser(new UserDto(null, "Booker", "booker@mail.ru"));
        stranger = userService.createUser(new UserDto(null, "Stranger", "stranger@mail.ru"));

        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);
        item = itemService.createItem(owner.getId(), dto);

        LocalDateTime now = LocalDateTime.now();
        pastBooking = approve(book(now.minusDays(5), now.minusDays(4)));
        currentBooking = approve(book(now.minusDays(1), now.plusDays(1)));
        futureBooking = book(now.plusDays(2), now.plusDays(3));
        rejectedBooking = bookingService.changeStatus(owner.getId(),
                book(now.plusDays(4), now.plusDays(5)).getId(), false);
    }

    private BookingDto book(LocalDateTime start, LocalDateTime end) {
        return bookingService.createBooking(booker.getId(), new NewBookingDto(item.getId(), start, end));
    }

    private BookingDto approve(BookingDto booking) {
        return bookingService.changeStatus(owner.getId(), booking.getId(), true);
    }

    private static Collection<Long> ids(Collection<BookingDto> bookings) {
        return bookings.stream().map(BookingDto::getId).toList();
    }

    @Test
    void createBookingHasWaitingStatus() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusDays(1);

        BookingDto booking = book(start, end);

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(Status.WAITING);
        assertThat(booking.getStart()).isEqualTo(start);
        assertThat(booking.getEnd()).isEqualTo(end);
        assertThat(booking.getItem().getId()).isEqualTo(item.getId());
        assertThat(booking.getItem().getName()).isEqualTo("Drill");
        assertThat(booking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(booking.getBooker().getName()).isEqualTo("Booker");
    }

    @Test
    void createBookingByUnknownUserThrowsNotFound() {
        NewBookingDto dto = new NewBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(999_999L, dto));
    }

    @Test
    void createBookingForUnknownItemThrowsNotFound() {
        NewBookingDto dto = new NewBookingDto(999_999L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(booker.getId(), dto));
    }

    @Test
    void createBookingOfOwnItemThrowsForbidden() {
        NewBookingDto dto = new NewBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(ForbiddenException.class, () -> bookingService.createBooking(owner.getId(), dto));
    }

    @Test
    void createBookingOfUnavailableItemThrowsBadRequest() {
        ItemDto patch = new ItemDto();
        patch.setAvailable(false);
        itemService.updateItem(owner.getId(), item.getId(), patch);
        NewBookingDto dto = new NewBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(booker.getId(), dto));
    }

    @Test
    void getByIdForBookerAndOwner() {
        assertThat(bookingService.getById(booker.getId(), futureBooking.getId()).getId())
                .isEqualTo(futureBooking.getId());
        assertThat(bookingService.getById(owner.getId(), futureBooking.getId()).getStatus())
                .isEqualTo(Status.WAITING);
    }

    @Test
    void getByIdForStrangerThrowsForbidden() {
        assertThrows(ForbiddenException.class,
                () -> bookingService.getById(stranger.getId(), futureBooking.getId()));
    }

    @Test
    void getByIdByUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.getById(999_999L, futureBooking.getId()));
    }

    @Test
    void getUnknownBookingThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.getById(booker.getId(), 999_999L));
    }

    @Test
    void changeStatusApprove() {
        BookingDto approved = approve(futureBooking);

        assertThat(approved.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(bookingService.getById(owner.getId(), futureBooking.getId()).getStatus())
                .isEqualTo(Status.APPROVED);
    }

    @Test
    void changeStatusReject() {
        BookingDto rejected = bookingService.changeStatus(owner.getId(), futureBooking.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void changeStatusByNotOwnerThrowsForbidden() {
        assertThrows(ForbiddenException.class,
                () -> bookingService.changeStatus(booker.getId(), futureBooking.getId(), true));
    }

    @Test
    void changeStatusTwiceThrowsConflict() {
        assertThrows(ConflictException.class,
                () -> bookingService.changeStatus(owner.getId(), pastBooking.getId(), false));
    }

    @Test
    void changeStatusOfUnknownBookingThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> bookingService.changeStatus(owner.getId(), 999_999L, true));
    }

    @Test
    void getUserBookingsAll() {
        Collection<BookingDto> bookings = bookingService.getUserBookings(booker.getId(), State.ALL);

        assertThat(ids(bookings)).containsExactly(
                rejectedBooking.getId(), futureBooking.getId(), currentBooking.getId(), pastBooking.getId());
    }

    @Test
    void getUserBookingsCurrent() {
        assertThat(ids(bookingService.getUserBookings(booker.getId(), State.CURRENT)))
                .containsExactly(currentBooking.getId());
    }

    @Test
    void getUserBookingsPast() {
        assertThat(ids(bookingService.getUserBookings(booker.getId(), State.PAST)))
                .containsExactly(pastBooking.getId());
    }

    @Test
    void getUserBookingsFuture() {
        assertThat(ids(bookingService.getUserBookings(booker.getId(), State.FUTURE)))
                .containsExactly(rejectedBooking.getId(), futureBooking.getId());
    }

    @Test
    void getUserBookingsWaiting() {
        assertThat(ids(bookingService.getUserBookings(booker.getId(), State.WAITING)))
                .containsExactly(futureBooking.getId());
    }

    @Test
    void getUserBookingsRejected() {
        assertThat(ids(bookingService.getUserBookings(booker.getId(), State.REJECTED)))
                .containsExactly(rejectedBooking.getId());
    }

    @Test
    void getUserBookingsOfUserWithoutBookingsIsEmpty() {
        assertThat(bookingService.getUserBookings(stranger.getId(), State.ALL)).isEmpty();
    }

    @Test
    void getUserBookingsByUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(999_999L, State.ALL));
    }

    @Test
    void getUserItemsBookingsAll() {
        assertThat(ids(bookingService.getUserItemsBookings(owner.getId(), State.ALL))).containsExactly(
                rejectedBooking.getId(), futureBooking.getId(), currentBooking.getId(), pastBooking.getId());
    }

    @Test
    void getUserItemsBookingsCurrent() {
        assertThat(ids(bookingService.getUserItemsBookings(owner.getId(), State.CURRENT)))
                .containsExactly(currentBooking.getId());
    }

    @Test
    void getUserItemsBookingsPast() {
        assertThat(ids(bookingService.getUserItemsBookings(owner.getId(), State.PAST)))
                .containsExactly(pastBooking.getId());
    }

    @Test
    void getUserItemsBookingsFuture() {
        assertThat(ids(bookingService.getUserItemsBookings(owner.getId(), State.FUTURE)))
                .containsExactly(rejectedBooking.getId(), futureBooking.getId());
    }

    @Test
    void getUserItemsBookingsWaiting() {
        assertThat(ids(bookingService.getUserItemsBookings(owner.getId(), State.WAITING)))
                .containsExactly(futureBooking.getId());
    }

    @Test
    void getUserItemsBookingsRejected() {
        assertThat(ids(bookingService.getUserItemsBookings(owner.getId(), State.REJECTED)))
                .containsExactly(rejectedBooking.getId());
    }

    @Test
    void getUserItemsBookingsOfUserWithoutItemsIsEmpty() {
        assertThat(bookingService.getUserItemsBookings(booker.getId(), State.ALL)).isEmpty();
    }

    @Test
    void getUserItemsBookingsByUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.getUserItemsBookings(999_999L, State.ALL));
    }
}
