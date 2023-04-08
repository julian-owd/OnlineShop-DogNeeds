package de.dogneeds.backend.product.product;

import de.dogneeds.backend.product.category.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@NoArgsConstructor
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "description", nullable = false, length = 512)
    private String description;

    @Column(name = "price", nullable = false, length = 64)
    private double price;

    @Column(name = "img", length = 128)
    private String img;

    @OneToOne
    @JoinColumn(name = "cid", referencedColumnName = "cid")
    private Category cid;

    public Product(String name, String description, double price, String img, Category cid) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.img = img;
        this.cid = cid;
    }
}