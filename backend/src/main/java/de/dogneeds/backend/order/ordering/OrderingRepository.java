package de.dogneeds.backend.order.ordering;

import de.dogneeds.backend.user.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface OrderingRepository extends CrudRepository<Ordering, Long> {
    @Query("SELECT o FROM Ordering o WHERE o.uid = ?1")
    Collection<Ordering> findOrderingsByUid(User user);

    @Query("SELECT o FROM Ordering o")
    Collection<Ordering> findAllOrders();

    @Query("SELECT o FROM Ordering o WHERE o.uid = ?1 AND o.oid = ?2")
    Ordering findOrderingByUidAndOid(User user, Long orderId);

    @Query("SELECT o FROM Ordering o WHERE o.oid = ?1")
    Ordering findOrderingByOid(Long orderId);
}
