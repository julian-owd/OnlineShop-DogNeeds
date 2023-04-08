package de.dogneeds.backend.user.user;

import de.dogneeds.backend.user.address.Address;
import de.dogneeds.backend.user.usergroup.UserGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user")
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(name = "email", nullable = false, length = 64)
    private String email;

    @Column(name = "password", nullable = false, length = 64)
    private String password;

    @Column(name = "firstName", nullable = false, length = 20)
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 20)
    private String lastName;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "aid", referencedColumnName = "aid")
    private Address aid;

    @OneToOne
    @JoinColumn(name = "ugid", referencedColumnName = "ugid")
    private UserGroup ugid;

    public User(String email, String password, String firstName, String lastName, Address aid, UserGroup ugid) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.aid = aid;
        this.ugid = ugid;
    }
}