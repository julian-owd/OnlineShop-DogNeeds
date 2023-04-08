package de.dogneeds.backend.product.feedback;

import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.user.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface FeedbackRepository extends CrudRepository<Feedback, Long> {
    @Query("SELECT f FROM Feedback f WHERE f.uid = ?1 AND f.pid = ?2")
    Feedback findFeedbackByUidAndPid(User user, Product product);

    @Query("SELECT f FROM Feedback f WHERE f.pid = ?1")
    Collection<Feedback> findFeedbacksByPid(Product product);

    @Query("SELECT f FROM Feedback f")
    Collection<Feedback> findAllFeedbacks();

    @Transactional
    @Modifying
    @Query("DELETE FROM Feedback f WHERE f.uid = ?1")
    void deleteAllByUid(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM Feedback f WHERE f.pid = ?1")
    void deleteAllByPid(Product product);
}
