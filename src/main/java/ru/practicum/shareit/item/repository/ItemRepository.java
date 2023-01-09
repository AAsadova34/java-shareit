package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@RepositoryRestResource
public interface ItemRepository extends JpaRepository<Item, Long> {
    Item save(Item item);

    Item getReferenceById(long itemId);

    boolean existsById(long itemId);

    List<Item> findAllByUserIdOrderById(long userId);

    @Query("SELECT new Item(i.id, i.userId, i.name, i.description, i.available) " +
            "FROM Item AS i " +
            "WHERE i.available = TRUE " +
            "AND (LOWER(i.name) LIKE %?1% OR LOWER(i.description) LIKE %?2%)")
    List<Item> findByNameOrDescription(String name, String description);
}
