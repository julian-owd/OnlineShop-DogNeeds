package de.dogneeds.backend.service;

import de.dogneeds.backend.events.DeleteEvent;
import de.dogneeds.backend.order.ordering.Ordering;
import de.dogneeds.backend.order.orderingpos.OrderingPos;
import de.dogneeds.backend.product.feedback.Feedback;
import de.dogneeds.backend.product.feedback.FeedbackRepository;
import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.user.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Service
public class FeedbackService implements ApplicationListener<DeleteEvent> {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * check if a user has bought a product already once
     *
     * @param session   the session of the user
     * @param productId the productId of the product
     * @return true if a purchase of this product was made already, false if not
     */
    public boolean hasBoughtProduct(UUID session, Long productId) {
        ArrayList<Ordering> orderings = orderService.getOrders(session);
        // invalid session
        if (orderings == null) {
            return false;
        }

        // no orders placed yet
        if (orderings.size() == 0) {
            return false;
        }

        for (Ordering ordering : orderings) {
            Collection<OrderingPos> positions = orderService.getOrderPositions(ordering);
            for (OrderingPos position : positions) {
                if (position.getPid().getPid().longValue() == productId) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * check if a user is allowed to give feedback to a product
     *
     * @param session   the session of the user
     * @param productId the productId of the product
     * @return true if the user is allowed to give feedback, false if not
     */
    public boolean isAllowedToGiveFeedback(UUID session, Long productId) {
        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return false;
        }
        Product product = productService.getProductRepository().getProductById(productId);
        if (product == null) {
            return false;
        }

        return feedbackRepository.findFeedbackByUidAndPid(user, product) == null;
    }

    /**
     * add a feedback
     *
     * @param session     the session of the user
     * @param productId   the productId of the product
     * @param rating      the rating (1-5)
     * @param description the optional description
     * @return true if the feedback was added, false if not
     */
    public boolean addFeedback(UUID session, Long productId, int rating, String description) {
        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return false;
        }
        Product product = productService.getProductRepository().getProductById(productId);
        if (product == null) {
            return false;
        }
        if (rating < 1 || rating > 5) {
            return false;
        }
        if (description.isEmpty() || description.isBlank()) {
            description = null;
        }

        System.out.println(user.getEmail() + " gave feedback to " + productId + " rating: " + rating);

        feedbackRepository.save(new Feedback(user, product, rating, description));
        return true;
    }

    /**
     * gets the feedback for a product
     *
     * @param productId the productId
     * @return collection of feedbacks
     */
    public Collection<Feedback> getFeedbacks(Long productId) {
        Product product = productService.getProductRepository().getProductById(productId);
        if (product == null) {
            return null;
        }

        return feedbackRepository.findFeedbacksByPid(product);
    }

    /**
     * returns a collection of all feedbacks
     *
     * @return collection
     */
    public Collection<Feedback> getAllFeedbacks() {
        Collection<Feedback> feedbacks = feedbackRepository.findAllFeedbacks();
        Collection<Feedback> result = new ArrayList<>();
        for (Feedback f1 : feedbacks) {
            Feedback feedback = null;
            int maxRating = 0;
            for (Feedback f2 : result) {
                if (f2.getUid().getUid().longValue() == f1.getUid().getUid()) {
                    feedback = f2;
                    maxRating = f2.getRating();
                }
            }
            if (feedback == null) {
                result.add(f1);
            } else {
                if (maxRating < f1.getRating()) {
                    result.add(f1);
                    result.remove(feedback);
                }
            }
        }

        return result;
    }

    /**
     * listen for delete event
     *
     * @param event event
     */
    @Override
    public void onApplicationEvent(DeleteEvent event) {
        if (event.getObject() instanceof User) {
            feedbackRepository.deleteAllByUid((User) event.getObject());
        } else if (event.getObject() instanceof Product) {
            feedbackRepository.deleteAllByPid((Product) event.getObject());
        }
    }
}
