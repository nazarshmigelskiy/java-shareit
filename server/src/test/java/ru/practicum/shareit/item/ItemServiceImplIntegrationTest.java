package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.NewBookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemRequestService itemRequestService;

    private UserDto owner;
    private UserDto booker;

    @BeforeEach
    void setUp() {
        owner = userService.createUser(new UserDto(null, "Owner", "owner@mail.ru"));
        booker = userService.createUser(new UserDto(null, "Booker", "booker@mail.ru"));
    }

    private ItemDto newItem(String name, String description, boolean available) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }

    private ItemDto createDrill() {
        return itemService.createItem(owner.getId(), newItem("Drill", "Powerful drill", true));
    }

    private BookingDto createApprovedBooking(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingDto booking = bookingService.createBooking(booker.getId(), new NewBookingDto(itemId, start, end));
        return bookingService.changeStatus(owner.getId(), booking.getId(), true);
    }

    @Test
    void createItemAndGetById() {
        ItemDto created = createDrill();

        ItemDto found = itemService.getById(booker.getId(), created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Drill");
        assertThat(found.getDescription()).isEqualTo("Powerful drill");
        assertThat(found.getAvailable()).isTrue();
        assertThat(found.getRequestId()).isNull();
        assertThat(found.getLastBooking()).isNull();
        assertThat(found.getNextBooking()).isNull();
        assertThat(found.getComments()).isEmpty();
    }

    @Test
    void createItemForRequest() {
        ItemRequestDto request = itemRequestService.createRequest(booker.getId(),
                new ItemRequestDto(null, "Need a drill", null, null, null));
        ItemDto dto = newItem("Drill", "Powerful drill", true);
        dto.setRequestId(request.getId());

        ItemDto created = itemService.createItem(owner.getId(), dto);

        assertThat(created.getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void createItemForUnknownRequestThrowsNotFound() {
        ItemDto dto = newItem("Drill", "Powerful drill", true);
        dto.setRequestId(999_999L);

        assertThrows(NotFoundException.class, () -> itemService.createItem(owner.getId(), dto));
    }

    @Test
    void createItemByUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemService.createItem(999_999L, newItem("Drill", "Powerful drill", true)));
    }

    @Test
    void getAllReturnsCreatedItems() {
        createDrill();
        itemService.createItem(booker.getId(), newItem("Saw", "Sharp saw", true));

        Collection<ItemDto> items = itemService.getAll();

        assertThat(items).extracting(ItemDto::getName).contains("Drill", "Saw");
    }

    @Test
    void getByIdForOwnerContainsLastAndNextBookings() {
        ItemDto item = createDrill();
        LocalDateTime now = LocalDateTime.now();
        BookingDto past = createApprovedBooking(item.getId(), now.minusDays(3), now.minusDays(2));
        BookingDto future = createApprovedBooking(item.getId(), now.plusDays(2), now.plusDays(3));

        ItemDto found = itemService.getById(owner.getId(), item.getId());

        assertThat(found.getLastBooking().getId()).isEqualTo(past.getId());
        assertThat(found.getLastBooking().getBookerId()).isEqualTo(booker.getId());
        assertThat(found.getNextBooking().getId()).isEqualTo(future.getId());
    }

    @Test
    void getByIdForNotOwnerHidesBookings() {
        ItemDto item = createDrill();
        LocalDateTime now = LocalDateTime.now();
        createApprovedBooking(item.getId(), now.minusDays(3), now.minusDays(2));

        ItemDto found = itemService.getById(booker.getId(), item.getId());

        assertThat(found.getLastBooking()).isNull();
        assertThat(found.getNextBooking()).isNull();
    }

    @Test
    void getByIdUnknownItemThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.getById(owner.getId(), 999_999L));
    }

    @Test
    void getByUserIdReturnsOwnerItemsWithComments() {
        ItemDto drill = createDrill();
        itemService.createItem(owner.getId(), newItem("Saw", "Sharp saw", true));
        itemService.createItem(booker.getId(), newItem("Hammer", "Heavy hammer", true));
        LocalDateTime now = LocalDateTime.now();
        createApprovedBooking(drill.getId(), now.minusDays(3), now.minusDays(2));
        itemService.addComment(booker.getId(), drill.getId(), new CommentDto(null, "Great drill", null, null));

        Collection<ItemDto> items = itemService.getByUserId(owner.getId());

        assertThat(items).extracting(ItemDto::getName).containsExactlyInAnyOrder("Drill", "Saw");
        ItemDto foundDrill = items.stream().filter(i -> i.getName().equals("Drill")).findFirst().orElseThrow();
        assertThat(foundDrill.getComments()).extracting(CommentDto::getText).containsExactly("Great drill");
        ItemDto foundSaw = items.stream().filter(i -> i.getName().equals("Saw")).findFirst().orElseThrow();
        assertThat(foundSaw.getComments()).isEmpty();
    }

    @Test
    void getByUserIdUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.getByUserId(999_999L));
    }

    @Test
    void updateItemChangesAllFields() {
        ItemDto item = createDrill();

        ItemDto updated = itemService.updateItem(owner.getId(), item.getId(),
                newItem("New drill", "Even more powerful", false));

        assertThat(updated.getName()).isEqualTo("New drill");
        assertThat(updated.getDescription()).isEqualTo("Even more powerful");
        assertThat(updated.getAvailable()).isFalse();
        ItemDto found = itemService.getById(owner.getId(), item.getId());
        assertThat(found.getName()).isEqualTo("New drill");
    }

    @Test
    void updateItemWithOnlyAvailableKeepsOtherFields() {
        ItemDto item = createDrill();
        ItemDto patch = new ItemDto();
        patch.setAvailable(false);

        ItemDto updated = itemService.updateItem(owner.getId(), item.getId(), patch);

        assertThat(updated.getName()).isEqualTo("Drill");
        assertThat(updated.getDescription()).isEqualTo("Powerful drill");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void updateItemByNotOwnerThrowsForbidden() {
        ItemDto item = createDrill();

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(booker.getId(), item.getId(), newItem("Hack", "Hack", true)));
    }

    @Test
    void updateItemByUnknownUserThrowsNotFound() {
        ItemDto item = createDrill();

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(999_999L, item.getId(), newItem("Hack", "Hack", true)));
    }

    @Test
    void updateUnknownItemThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(owner.getId(), 999_999L, newItem("Hack", "Hack", true)));
    }

    @Test
    void deleteItemRemovesItem() {
        ItemDto item = createDrill();

        itemService.deleteItem(owner.getId(), item.getId());

        assertThrows(NotFoundException.class, () -> itemService.getById(owner.getId(), item.getId()));
    }

    @Test
    void deleteItemByNotOwnerThrowsForbidden() {
        ItemDto item = createDrill();

        assertThrows(ForbiddenException.class, () -> itemService.deleteItem(booker.getId(), item.getId()));
    }

    @Test
    void deleteUnknownItemThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.deleteItem(owner.getId(), 999_999L));
    }

    @Test
    void searchFindsAvailableItemsByNameAndDescriptionIgnoringCase() {
        createDrill();
        itemService.createItem(owner.getId(), newItem("Screwdriver", "Cordless DRILL driver", true));
        itemService.createItem(owner.getId(), newItem("Old drill", "Broken", false));
        itemService.createItem(owner.getId(), newItem("Saw", "Sharp saw", true));

        Collection<ItemDto> found = itemService.search("dRiLl");

        assertThat(found).extracting(ItemDto::getName).containsExactlyInAnyOrder("Drill", "Screwdriver");
    }

    @Test
    void searchWithBlankTextReturnsEmptyList() {
        createDrill();

        assertThat(itemService.search(" ")).isEmpty();
        assertThat(itemService.search(null)).isEmpty();
    }

    @Test
    void addCommentAfterFinishedBooking() {
        ItemDto item = createDrill();
        LocalDateTime now = LocalDateTime.now();
        createApprovedBooking(item.getId(), now.minusDays(3), now.minusDays(2));

        CommentDto comment = itemService.addComment(booker.getId(), item.getId(),
                new CommentDto(null, "Great drill", null, null));

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getText()).isEqualTo("Great drill");
        assertThat(comment.getAuthorName()).isEqualTo("Booker");
        assertThat(comment.getCreated()).isNotNull();
        ItemDto found = itemService.getById(booker.getId(), item.getId());
        assertThat(found.getComments()).extracting(CommentDto::getText).containsExactly("Great drill");
    }

    @Test
    void addCommentWithoutFinishedBookingThrowsBadRequest() {
        ItemDto item = createDrill();
        LocalDateTime now = LocalDateTime.now();
        createApprovedBooking(item.getId(), now.plusDays(1), now.plusDays(2));

        assertThrows(BadRequestException.class, () -> itemService.addComment(booker.getId(), item.getId(),
                new CommentDto(null, "Great drill", null, null)));
    }
}
