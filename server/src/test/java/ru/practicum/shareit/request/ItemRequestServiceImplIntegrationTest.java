package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemRequestServiceImplIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    private UserDto requestor;
    private UserDto owner;

    @BeforeEach
    void setUp() {
        requestor = userService.createUser(new UserDto(null, "Requestor", "requestor@mail.ru"));
        owner = userService.createUser(new UserDto(null, "Owner", "owner@mail.ru"));
    }

    private ItemRequestDto createRequest(UserDto user, String description) {
        return itemRequestService.createRequest(user.getId(), new ItemRequestDto(null, description, null, null, null));
    }

    private ItemDto createItemForRequest(Long requestId, String name) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription("Answer to request");
        dto.setAvailable(true);
        dto.setRequestId(requestId);
        return itemService.createItem(owner.getId(), dto);
    }

    @Test
    void createRequestFillsRequestorAndCreated() {
        ItemRequestDto created = createRequest(requestor, "Need a drill");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a drill");
        assertThat(created.getRequestor()).isEqualTo(requestor.getId());
        assertThat(created.getCreated()).isNotNull();
    }

    @Test
    void createRequestByUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(999_999L,
                new ItemRequestDto(null, "Need a drill", null, null, null)));
    }

    @Test
    void getRequestByIdContainsItemsAnsweringRequest() {
        ItemRequestDto request = createRequest(requestor, "Need a drill");
        ItemDto item = createItemForRequest(request.getId(), "Drill");

        ItemRequestDto found = itemRequestService.getRequestById(request.getId());

        assertThat(found.getDescription()).isEqualTo("Need a drill");
        assertThat(found.getItems()).hasSize(1);
        ResponseDto response = found.getItems().iterator().next();
        assertThat(response.getItemId()).isEqualTo(item.getId());
        assertThat(response.getName()).isEqualTo("Drill");
        assertThat(response.getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void getRequestByIdWithoutItemsHasEmptyItems() {
        ItemRequestDto request = createRequest(requestor, "Need a drill");

        assertThat(itemRequestService.getRequestById(request.getId()).getItems()).isEmpty();
    }

    @Test
    void getUnknownRequestThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(999_999L));
    }

    @Test
    void getUserRequestsReturnsOnlyOwnRequestsWithItems() {
        ItemRequestDto drill = createRequest(requestor, "Need a drill");
        ItemRequestDto saw = createRequest(requestor, "Need a saw");
        createRequest(owner, "Need a hammer");
        createItemForRequest(drill.getId(), "Drill");

        Collection<ItemRequestDto> requests = itemRequestService.getUserRequests(requestor.getId());

        assertThat(requests).extracting(ItemRequestDto::getId)
                .containsExactlyInAnyOrder(drill.getId(), saw.getId());
        ItemRequestDto foundDrill = requests.stream()
                .filter(r -> r.getId().equals(drill.getId())).findFirst().orElseThrow();
        assertThat(foundDrill.getItems()).extracting(ResponseDto::getName).containsExactly("Drill");
        ItemRequestDto foundSaw = requests.stream()
                .filter(r -> r.getId().equals(saw.getId())).findFirst().orElseThrow();
        assertThat(foundSaw.getItems()).isEmpty();
    }

    @Test
    void getUserRequestsWithoutRequestsIsEmpty() {
        assertThat(itemRequestService.getUserRequests(requestor.getId())).isEmpty();
    }

    @Test
    void getUserRequestsByUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getUserRequests(999_999L));
    }

    @Test
    void getAllRequestsReturnsRequestsOfOtherUsers() {
        ItemRequestDto own = createRequest(requestor, "Need a drill");
        ItemRequestDto foreign = createRequest(owner, "Need a hammer");

        Collection<ItemRequestDto> requests = itemRequestService.getAllRequests(requestor.getId());

        assertThat(requests).extracting(ItemRequestDto::getId)
                .contains(foreign.getId())
                .doesNotContain(own.getId());
    }

    @Test
    void getAllRequestsByUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getAllRequests(999_999L));
    }
}
