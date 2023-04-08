package de.dogneeds.backend.user.usergroup;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_group")
@NoArgsConstructor
@Getter
@Setter
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ugid;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    public UserGroup(String name) {
        this.name = name;
    }
}
