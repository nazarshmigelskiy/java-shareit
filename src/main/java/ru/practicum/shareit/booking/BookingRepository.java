package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findAllByBookerIdOrderByStartDesc(Long userId);

    Collection<Booking> findAllByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long userId,
            Long itemId,
            Status status,
            LocalDateTime time
    );

    Collection<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(
            Long userId,
            LocalDateTime now
    );

    Collection<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(
            Long userId,
            LocalDateTime now
    );

    Collection<Booking> findAllByBookerIdAndStatusOrderByStartDesc(
            Long userId,
            Status status
    );

    Collection<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    Collection<Booking>
    findAllByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long ownerId,
            LocalDateTime start,
            LocalDateTime end
    );

    Collection<Booking>
    findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId,
            LocalDateTime now
    );

    Collection<Booking>
    findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId,
            LocalDateTime now
    );

    Collection<Booking>
    findAllByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId,
            Status status
    );

    Optional<Booking> findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
            Long itemId,
            Status status,
            LocalDateTime now
    );

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId,
            Status status,
            LocalDateTime now
    );
}
