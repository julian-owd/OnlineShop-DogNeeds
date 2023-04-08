package de.dogneeds.backend.order.shoppingcart;

import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.user.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shopping_cart")
@NoArgsConstructor
@Getter
@Setter
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scid;

    @OneToOne
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private User uid;

    @OneToOne
    @JoinColumn(name = "pid", referencedColumnName = "pid")
    private Product pid;

    @Column(name = "amount", nullable = false, length = 10)
    private int amount;

    public ShoppingCart(User uid, Product pid) {
        this.uid = uid;
        this.pid = pid;
        this.amount = 1;
    }

    /**
     * increases the amount by 1
     */
    public void increaseAmount() {
        this.amount++;
    }

    /**
     * decreases the amount by 1
     */
    public void decreaseAmount() {
        if (amount > 1) {
            this.amount--;
        }
    }

    /**
     * returns a String for the receipt
     *
     * @return position line
     */
    public String getProductString() {
        return String.format("- " + this.getAmount() + "x " + this.getPid().getName() + ", %.2f" + "€", this.getPid().getPrice());
    }
}
