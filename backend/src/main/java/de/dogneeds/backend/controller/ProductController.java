package de.dogneeds.backend.controller;

import de.dogneeds.backend.service.ProductService;
import de.dogneeds.backend.service.UserService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Controller
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    /**
     * loads all existing products
     *
     * @return list of products in json format
     */
    @CrossOrigin
    @RequestMapping("/get")
    public String getProducts() {
        return new JSONArray(productService.getProducts()).toString();
    }

    /**
     * loads the top 3 best-selling products
     *
     * @return list of products in json format
     */
    @CrossOrigin
    @RequestMapping("/getBest")
    public String getBestseller() {
        return new JSONArray(productService.getBestseller()).toString();
    }

    @CrossOrigin
    @RequestMapping("/request")
    public String getDetails(@RequestBody String requestString) {
        String[] requestUnfiltered = requestString.split(";");
        Long productId = Long.parseLong(requestUnfiltered[0]);

        // remove productId from request
        String[] requestFiltered = new String[requestUnfiltered.length - 1];
        System.arraycopy(requestUnfiltered, 1, requestFiltered, 0, requestUnfiltered.length - 1);

        return productService.request(productId, requestFiltered);
    }

    /**
     * handles requests to add a product
     *
     * @param data the data of the product (name, price, ...)
     * @param file optional the picture of the product
     * @return success if valid, error if invalid
     */
    @CrossOrigin
    @RequestMapping("/add")
    public String addProduct(@RequestParam("data") String data, @RequestParam(value = "image", required = false) MultipartFile file) {
        JSONObject jsonObject = new JSONObject(data);

        // the needed arguments are not given
        if (!jsonObject.has("session") || !jsonObject.has("category") || !jsonObject.has("name") || !jsonObject.has("description") || !jsonObject.has("price")) {
            return "error;wrongargs";
        }

        // permission check
        if (!userService.isAdmin(UUID.fromString(jsonObject.getString("session")))) {
            return "error;nopermission";
        }

        return productService.addProduct(jsonObject, file);
    }

    /**
     * handles requests to edit a product
     *
     * @param changes session and the changed attributes
     * @param file    optional a new image
     * @return success/error depending on if the action was successful
     */
    @CrossOrigin
    @RequestMapping("/edit")
    public String editProduct(@RequestParam("changes") String changes, @RequestParam(value = "image", required = false) MultipartFile file) {
        JSONObject jsonObject = new JSONObject(changes);

        // the needed arguments are not given
        if (!jsonObject.has("session") || !jsonObject.has("productId")) {
            return "error;wrongargs";
        }

        // permission check
        if (!userService.isAdmin(UUID.fromString(String.valueOf(jsonObject.get("session"))))) {
            return "error;nopermission";
        }

        return productService.editProduct(jsonObject, file);
    }

    /**
     * handles requests to remove a product
     *
     * @param details session and the productId
     * @return success/error depending on if the action was successful
     */
    @CrossOrigin
    @RequestMapping("/delete")
    public String deleteProduct(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        // the needed arguments are not given
        if (!jsonObject.has("session") || !jsonObject.has("productId")) {
            return "error;wrongargs";
        }

        // permission check
        if (!userService.isAdmin(UUID.fromString(String.valueOf(jsonObject.get("session"))))) {
            return "error;nopermission";
        }

        return productService.deleteProduct(jsonObject.getLong("productId"));
    }

    /**
     * handles requests to load a picture
     *
     * @param productId the id of the product
     * @return byte array of image or null
     */
    @CrossOrigin
    @RequestMapping("/img")
    public byte[] getImage(@RequestBody String productId) {
        return productService.getImage(Long.parseLong(productId));
    }
}