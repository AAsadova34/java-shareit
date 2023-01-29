package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@RepositoryRestResource
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);

    User getReferenceById(long id);

    boolean existsById(long id);

    void deleteById(long id);

    List<User> findAll();
}
