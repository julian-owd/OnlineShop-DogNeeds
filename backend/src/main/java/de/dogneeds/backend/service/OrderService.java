package de.dogneeds.backend.service;

import de.dogneeds.backend.events.DeleteEvent;
import de.dogneeds.backend.order.ordering.Ordering;
import de.dogneeds.backend.order.ordering.OrderingRepository;
import de.dogneeds.backend.order.orderingpos.OrderingPos;
import de.dogneeds.backend.order.orderingpos.OrderingPosRepository;
import de.dogneeds.backend.order.shoppingcart.ShoppingCart;
import de.dogneeds.backend.order.shoppingcart.ShoppingCartRepository;
import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.user.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
public class OrderService implements ApplicationListener<DeleteEvent> {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private OrderingRepository orderingRepository;

    @Autowired
    private OrderingPosRepository orderingPosRepository;

    @Autowired
    private EmailService emailService;

    /**
     * handles requests to get the shopping cart of a user
     *
     * @param session session of the user
     * @return collection of shopping carts
     */
    public Collection<ShoppingCart> getShoppingCartProducts(UUID session) {
        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return null;
        }

        return shoppingCartRepository.getShoppingCartsByUid(user);
    }

    /**
     * get the orders of a user
     *
     * @param session session of the user
     * @return collection of orders
     */
    public ArrayList<Ordering> getOrders(UUID session) {
        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return null;
        }

        ArrayList<Ordering> orders = new ArrayList<>(orderingRepository.findOrderingsByUid(user));
        Collections.reverse(orders);

        return orders;
    }

    /**
     * get the orders of a user, requested by admin
     *
     * @param session session of the admin
     * @param userId  the id of the user to get the orders of
     * @return collection of orders
     */
    public ArrayList<Ordering> getOrders(UUID session, Long userId) {
        if (!userService.isAdmin(session)) {
            return null;
        }

        User user = userService.getUserRepository().getUserById(userId);

        ArrayList<Ordering> orders = new ArrayList<>(orderingRepository.findOrderingsByUid(user));
        Collections.reverse(orders);

        return orders;
    }

    /**
     * get the order of a user
     *
     * @param session session of the user
     * @param orderId oderId of the order
     * @return object of order or null
     */
    public Ordering getOrder(UUID session, long orderId) {
        if (userService.isAdmin(session)) {
            return orderingRepository.findOrderingByOid(orderId);
        }

        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return null;
        }

        return orderingRepository.findOrderingByUidAndOid(user, orderId);
    }

    /**
     * get the positions of an order
     *
     * @param ordering the order
     * @return collection of positions
     */
    public Collection<OrderingPos> getOrderPositions(Ordering ordering) {
        return orderingPosRepository.findOrderingPosByOid(ordering);
    }

    /**
     * adds a product to the shopping cart of the user
     * increases amount if the product is already added
     *
     * @param uuid      session of the user
     * @param productId productId of the product
     * @return success/error
     */
    public String addToShoppingCart(UUID uuid, Long productId) {
        User user = userService.getSessionMap().get(uuid);
        Product product = productService.getProductRepository().getProductById(productId);
        if (user == null) {
            return "error;invalidsession";
        }
        if (product == null) {
            return "error;productnotfound";
        }

        ShoppingCart shoppingCart = shoppingCartRepository.getShoppingCartByUidAndPid(user, product);
        // product is not yet in shopping cart
        if (shoppingCart == null) {
            shoppingCart = new ShoppingCart(user, product);
        } else {
            // increase amount
            shoppingCart.increaseAmount();
        }
        System.out.println("Updated shopping cart of " + user.getEmail());
        shoppingCartRepository.save(shoppingCart);
        return "success";
    }

    /**
     * decreases the amount of a product from the shopping cart
     *
     * @param uuid      session of the user
     * @param productId productId of the product
     * @return success/error
     */
    public String decreaseShoppingCart(UUID uuid, Long productId) {
        User user = userService.getSessionMap().get(uuid);
        Product product = productService.getProductRepository().getProductById(productId);
        if (user == null) {
            return "error;invalidsession";
        }
        if (product == null) {
            return "error;productnotfound";
        }

        ShoppingCart shoppingCart = shoppingCartRepository.getShoppingCartByUidAndPid(user, product);
        // product is not in shopping cart
        if (shoppingCart == null) {
            return "error;invalidcart";
        }

        // decrease amount
        shoppingCart.decreaseAmount();

        System.out.println("Updated shopping cart of " + user.getEmail());
        shoppingCartRepository.save(shoppingCart);
        return "success";
    }

    /**
     * delete a product from the shopping cart
     *
     * @param uuid      session of the user
     * @param productId productId of the product
     * @return success/error
     */
    public String deleteShoppingCart(UUID uuid, Long productId) {
        User user = userService.getSessionMap().get(uuid);
        Product product = productService.getProductRepository().getProductById(productId);
        if (user == null) {
            return "error;invalidsession";
        }
        if (product == null) {
            return "error;productnotfound";
        }

        ShoppingCart shoppingCart = shoppingCartRepository.getShoppingCartByUidAndPid(user, product);

        // product is not in shopping cart
        if (shoppingCart == null) {
            return "error;invalidcart";
        }
        System.out.println("Updated shopping cart of " + user.getEmail());
        shoppingCartRepository.delete(shoppingCart);
        return "success";
    }

    /**
     * create a new order and send receipt to user
     *
     * @param session session of the user checking out
     * @return success/error
     */
    public String checkout(UUID session) {
        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return "error;invalidsession";
        }

        Collection<ShoppingCart> shoppingCart = shoppingCartRepository.getShoppingCartsByUid(user);

        if (shoppingCart.size() == 0) {
            return "error;emptycart";
        }

        Ordering ordering = new Ordering(user);
        orderingRepository.save(ordering);

        String orderString = getOrderString(session);

        new Thread(() -> emailService.sendEmail(user.getEmail(), "Bestellbestätigung Nr.: " + ordering.getOid(), "Hallo " + user.getFirstName() + ",\nes freut uns sehr, dass du bei DogNeeds folgendes bestellt hast:\n" + orderString)).start();

        for (ShoppingCart cartItem : shoppingCart) {
            orderingPosRepository.save(new OrderingPos(cartItem.getAmount(), cartItem.getPid().getPrice(), cartItem.getPid(), ordering));
            deleteShoppingCart(session, cartItem.getPid().getPid());
        }

        System.out.println("Created order " + ordering.getOid());
        return "success";
    }

    /**
     * get all products from the shopping cart and return it in a String
     *
     * @param session the session of the user
     * @return list of products in string format
     */
    public String getOrderString(UUID session) {
        double price = 0;
        String returnString;
        ArrayList<String> productList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        User user = userService.getSessionMap().get(session);
        Collection<ShoppingCart> shoppingCart = shoppingCartRepository.getShoppingCartsByUid(user);

        for (ShoppingCart cartItem : shoppingCart) {
            productList.add(cartItem.getProductString());
            price = price + cartItem.getAmount() * cartItem.getPid().getPrice();
        }

        for (String s : productList) {
            sb.append(s);
            sb.append("\n");
        }

        returnString = String.format(sb + "\nBetrag: %.2f" + "€", price);
        return returnString;
    }

    /**
     * listen for delete event
     *
     * @param event event
     */
    @Override
    public void onApplicationEvent(DeleteEvent event) {
        if (event.getObject() instanceof User) {
            for (Ordering ordering : orderingRepository.findOrderingsByUid((User) event.getObject())) {
                orderingPosRepository.deleteAllByOid(ordering);
                orderingRepository.delete(ordering);
            }
            shoppingCartRepository.deleteAllByUid((User) event.getObject());
        } else if (event.getObject() instanceof Product) {
            orderingPosRepository.removeProduct((Product) event.getObject());
            shoppingCartRepository.deleteAllByPid((Product) event.getObject());
        }
    }
}
