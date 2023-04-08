package de.dogneeds.backend.chat.message;

import de.dogneeds.backend.chat.room.Room;
import de.dogneeds.backend.user.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "message")
@NoArgsConstructor
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mid;

    @Column(name = "text", nullable = false, length = 500)
    private String text;

    @Column(name = "type", length = 25)
    private String type;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    private Date timestamp;

    @OneToOne
    @JoinColumn(name = "sender", referencedColumnName = "uid")
    private User sender;

    @OneToOne
    @JoinColumn(name = "room", referencedColumnName = "rid")
    private Room room;

    public Message(String text, String type, User sender, Room room) {
        this.text = text;
        this.type = type;
        this.sender = sender;
        this.room = room;
    }
}
