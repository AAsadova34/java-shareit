package ru.practicum.shareit.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.practicum.shareit.request.model.ItemRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RepositoryRestResource
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    ItemRequest save(ItemRequest itemRequest);

    boolean existsById(long id);

    ItemRequest getReferenceById(long itemRequestId);

    List<ItemRequest> findItemRequestByRequestorIdOrderByCreatedDesc(long requestorId);

    @Query("SELECT new ItemRequest(ir.id, ir.description, ir.requestor, ir.created) " +
            "FROM ItemRequest AS ir " +
            "WHERE ir.requestor.id <> ?1 " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllOtherRequests(long userId);

    @Query("SELECT new ItemRequest(ir.id, ir.description, ir.requestor, ir.created) " +
            "FROM ItemRequest AS ir " +
            "WHERE ir.requestor.id <> ?1 " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllOtherRequests(long userId, Pageable pageable);
}
