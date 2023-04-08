package de.dogneeds.backend.user.address;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "address")
@NoArgsConstructor
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aid;

    @Column(name = "plz", nullable = false, length = 5)
    private String plz;

    @Column(name = "city", nullable = false, length = 20)
    private String city;

    @Column(name = "street", nullable = false, length = 20)
    private String street;

    @Column(name = "number", nullable = false, length = 5)
    private String number;

    public Address(String plz, String city, String street, String number) {
        this.plz = plz;
        this.city = city;
        this.street = street;
        this.number = number;
    }
}