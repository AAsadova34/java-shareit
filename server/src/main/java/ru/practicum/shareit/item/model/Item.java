package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Table(name = "items")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Boolean available;

    @Column(name = "request_id")
    private Long requestId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        return id != null && id.equals(((Item) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
