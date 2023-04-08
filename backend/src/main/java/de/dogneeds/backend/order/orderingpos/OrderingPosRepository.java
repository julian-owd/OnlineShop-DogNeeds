package de.dogneeds.backend.order.orderingpos;

import de.dogneeds.backend.order.ordering.Ordering;
import de.dogneeds.backend.product.product.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface OrderingPosRepository extends CrudRepository<OrderingPos, Long> {
    @Query("SELECT op FROM OrderingPos op WHERE op.oid = ?1")
    Collection<OrderingPos> findOrderingPosByOid(Ordering ordering);

    @Transactional
    @Modifying
    @Query("DELETE FROM OrderingPos op WHERE op.oid = ?1")
    void deleteAllByOid(Ordering ordering);

    @Transactional
    @Modifying
    @Query("UPDATE OrderingPos op SET op.pid = null WHERE op.pid = ?1")
    void removeProduct(Product product);
}