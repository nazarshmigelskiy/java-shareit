package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Collection<Item> findAllByOwnerId(Long userId);

    @Query("""
            SELECT i
            FROM Item i
            WHERE i.available = true
              AND (
                  LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%'))
                  OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))
              )
            """)
    Collection<Item> search(@Param("text") String text);
}
