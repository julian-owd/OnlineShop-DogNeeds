package de.dogneeds.backend.user.lastviewed;

import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.user.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "last_viewed")
@NoArgsConstructor
@Getter
@Setter
public class LastViewed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lwid;

    @OneToOne
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private User uid;

    @OneToOne
    @JoinColumn(name = "pid", referencedColumnName = "pid")
    private Product pid;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    private Date timestamp;

    public LastViewed(User uid, Product pid) {
        this.uid = uid;
        this.pid = pid;
    }
}
