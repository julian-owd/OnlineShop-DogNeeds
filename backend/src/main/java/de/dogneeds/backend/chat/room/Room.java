package de.dogneeds.backend.chat.room;


import de.dogneeds.backend.user.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "room")
@NoArgsConstructor
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rid;

    @Column(name = "open", nullable = false, length = 20)
    private boolean open;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    private Date timestamp;

    @OneToOne
    @JoinColumn(name = "creator", referencedColumnName = "uid")
    private User creator;

    @OneToOne
    @JoinColumn(name = "supporter", referencedColumnName = "uid")
    private User supporter;

    public Room(User creator) {
        this.creator = creator;
        this.open = true;
        this.supporter = null;
    }
}
