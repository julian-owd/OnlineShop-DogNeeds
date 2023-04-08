package de.dogneeds.backend.product.feedback;

import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.user.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "feedback")
@NoArgsConstructor
@Getter
@Setter
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fid;

    @OneToOne
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private User uid;

    @OneToOne
    @JoinColumn(name = "pid", referencedColumnName = "pid")
    private Product pid;

    @Column(name = "rating", nullable = false, length = 10)
    private int rating;

    @Column(name = "description", length = 100)
    private String description;

    public Feedback(User uid, Product pid, int rating, String description) {
        this.uid = uid;
        this.pid = pid;
        this.rating = rating;
        this.description = description;
    }
}
