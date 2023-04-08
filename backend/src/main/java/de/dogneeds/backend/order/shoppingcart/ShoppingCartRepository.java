package de.dogneeds.backend.order.shoppingcart;


import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.user.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface ShoppingCartRepository extends CrudRepository<ShoppingCart, Long> {
    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.uid = ?1 AND sc.pid = ?2")
    ShoppingCart getShoppingCartByUidAndPid(User user, Product product);

    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.uid = ?1")
    Collection<ShoppingCart> getShoppingCartsByUid(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM ShoppingCart sc WHERE sc.uid = ?1")
    void deleteAllByUid(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM ShoppingCart sc WHERE sc.pid = ?1")
    void deleteAllByPid(Product product);
}
