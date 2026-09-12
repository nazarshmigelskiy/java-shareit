package ru.practicum.shareit.item;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingShortDto;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public Collection<ItemDto> getAll() {
        return itemRepository.findAll().stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public ItemDto getById(Long userId, Long id) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Предмет с таким id не найден"));
        ItemDto itemDto = ItemMapper.toItemDto(item);
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            Optional<Booking> lastBooking =
                    bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                            id,
                            Status.APPROVED,
                            now
                    );
            Optional<Booking> nextBooking =
                    bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            id,
                            Status.APPROVED,
                            now
                    );
            itemDto.setLastBooking(lastBooking.map(this::toShortBooking).orElse(null));
            itemDto.setNextBooking(nextBooking.map(this::toShortBooking).orElse(null));
        }

        Collection<CommentDto> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(id).stream()
                .map(CommentMapper::toCommentDto)
                .toList();
        itemDto.setComments(comments);

        return itemDto;

    }

    public Collection<ItemDto> getByUserId(Long userId) {

        return itemRepository.findAllByOwnerId(userId)
                .stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.toItemDto(item);
                    Collection<CommentDto> comments =
                            commentRepository
                                    .findAllByItemIdOrderByCreatedDesc(item.getId())
                                    .stream()
                                    .map(CommentMapper::toCommentDto)
                                    .toList();
                    itemDto.setComments(comments);
                    return itemDto;
                })
                .toList();
    }

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        User owner = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с таким id не найден"));
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с таким id не найден"));
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Предмет с таким id не найден"));

        if (!item.getOwner().getId().equals(userId))
            throw new ForbiddenException("Редактировать страницу вещи может только владелец");

        if (itemDto.getName() != null) item.setName(itemDto.getName());

        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());

        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        return ItemMapper.toItemDto(item);
    }

    public void deleteItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Предмет с таким id не найден"));

        if (!item.getOwner().getId().equals(userId))
            throw new ForbiddenException("Удалить страницу вещи может только владелец");

        itemRepository.delete(item);
    }

    public Collection<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        boolean canComment = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, Status.APPROVED, LocalDateTime.now());

        if (!canComment) throw new BadRequestException("Пользователь не может оставить комментарий");

        User author = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с таким id не найден"));
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Предмет с таким id не найден"));

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private BookingShortDto toShortBooking(Booking booking) {
        if (booking == null) return null;

        return new BookingShortDto(booking.getId(), booking.getBooker().getId());
    }
}
