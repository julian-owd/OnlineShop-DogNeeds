package de.dogneeds.backend.order.orderingpos;

import de.dogneeds.backend.order.ordering.Ordering;
import de.dogneeds.backend.product.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ordering_pos")
@NoArgsConstructor
@Getter
@Setter
public class OrderingPos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int opid;

    @Column(name = "amount", nullable = false, length = 64)
    private int amount;

    @Column(name = "price", nullable = false, length = 64)
    private double price;

    @OneToOne
    @JoinColumn(name = "pid", referencedColumnName = "pid")
    private Product pid;

    @OneToOne
    @JoinColumn(name = "oid", referencedColumnName = "oid")
    private Ordering oid;

    public OrderingPos(int amount, double price, Product pid, Ordering oid) {
        this.amount = amount;
        this.price = price;
        this.pid = pid;
        this.oid = oid;
    }
}
