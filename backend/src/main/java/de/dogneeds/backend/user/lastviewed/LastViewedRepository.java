package de.dogneeds.backend.user.lastviewed;

import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.user.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface LastViewedRepository extends CrudRepository<LastViewed, Long> {
    @Query("SELECT lw FROM LastViewed lw WHERE lw.uid = ?1 ORDER BY lw.timestamp DESC")
    Collection<LastViewed> getLastViewedByUid(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM LastViewed lw WHERE lw.uid = ?1")
    void deleteAllByUid(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM LastViewed lw WHERE lw.pid = ?1")
    void deleteAllByPid(Product product);
}
