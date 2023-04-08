package de.dogneeds.backend.controller;

import de.dogneeds.backend.order.ordering.Ordering;
import de.dogneeds.backend.order.orderingpos.OrderingPos;
import de.dogneeds.backend.order.shoppingcart.ShoppingCart;
import de.dogneeds.backend.service.OrderService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Controller
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * get the cart of a user
     *
     * @param details session of the user
     * @return json array with products and amount
     */
    @CrossOrigin
    @RequestMapping("/getCartProducts")
    public String getCartProducts(@RequestBody String details) {
        Collection<ShoppingCart> cartItems = orderService.getShoppingCartProducts(UUID.fromString(details));

        ArrayList<JSONObject> result = new ArrayList<>();
        for (ShoppingCart shoppingCart : cartItems) {
            JSONObject jsonObject = new JSONObject(shoppingCart.getPid());
            jsonObject.put("amount", shoppingCart.getAmount());
            result.add(jsonObject);
        }

        return new JSONArray(result).toString();
    }

    /**
     * handles requests to load the orders of a user
     *
     * @param details the session and userId
     * @return json array of order ids
     */
    @CrossOrigin
    @RequestMapping("/getOrders")
    public String getOrders(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);
        if (!jsonObject.has("session")) {
            return null;
        }
        Collection<Ordering> orders;
        if (jsonObject.has("userId")) {
            orders = orderService.getOrders(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("userId"));
        } else {
            orders = orderService.getOrders(UUID.fromString(jsonObject.getString("session")));
        }

        if (orders == null) {
            return "error;noorders";
        }

        ArrayList<Long> result = new ArrayList<>();
        for (Ordering order : orders) {
            result.add(order.getOid());
        }

        return new JSONArray(result).toString();
    }

    /**
     * handles requests to load an order of a user
     *
     * @param details session and orderId
     * @return json array of order details
     */
    @CrossOrigin
    @RequestMapping("/getOrder")
    public String getOrder(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        if (!jsonObject.has("session") || !jsonObject.has("orderId")) {
            return "error;wrongargs";
        }

        Ordering ordering = orderService.getOrder(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("orderId"));
        if (ordering == null) {
            return "error;notfound";
        }

        Collection<OrderingPos> orderingPositions = orderService.getOrderPositions(ordering);
        double price = 0;
        for (OrderingPos op : orderingPositions) {
            price += op.getPrice() * op.getAmount();
        }

        JSONObject result = new JSONObject();
        result.put("timestamp", ordering.getTimestamp());
        result.put("price", price);
        result.put("products", orderingPositions);

        return result.toString();
    }

    /**
     * add a product to the cart or increase its amount
     *
     * @param details session and id of the product
     * @return success/error
     */
    @CrossOrigin
    @RequestMapping("/addCart")
    public String addProductToCart(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);
        System.out.println(jsonObject);

        if (!jsonObject.has("session") || !jsonObject.has("productId")) {
            return "error;wrongargs";
        }

        return orderService.addToShoppingCart(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("productId"));
    }

    /**
     * decrease the amount a product in the cart
     *
     * @param details session and id of the product
     * @return success/error
     */
    @CrossOrigin
    @RequestMapping("/decreaseCart")
    public String decreaseProductInCart(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);
        System.out.println(jsonObject);

        if (!jsonObject.has("session") || !jsonObject.has("productId")) {
            return "error;wrongargs";
        }

        return orderService.decreaseShoppingCart(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("productId"));
    }

    /**
     * remove a product from the cart
     *
     * @param details session and id of the product
     * @return success/error
     */
    @CrossOrigin
    @RequestMapping("/deleteCart")
    public String deleteProductFromCart(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);
        System.out.println(jsonObject);

        if (!jsonObject.has("session") || !jsonObject.has("productId")) {
            return "error;wrongargs";
        }

        return orderService.deleteShoppingCart(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("productId"));
    }

    /**
     * handles requests to create an order
     *
     * @param session session of the user
     * @return success/error
     */
    @CrossOrigin
    @RequestMapping("/checkout")
    public String checkout(@RequestBody String session) {
        return orderService.checkout(UUID.fromString(session));
    }

}
