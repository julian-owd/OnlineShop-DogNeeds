package de.dogneeds.backend.order.ordering;

import de.dogneeds.backend.user.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "ordering")
@NoArgsConstructor
@Getter
@Setter
public class Ordering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long oid;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    private Date timestamp;

    @OneToOne
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private User uid;

    public Ordering(User uid) {
        this.uid = uid;
    }
}
