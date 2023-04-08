package de.dogneeds.backend.controller;

import de.dogneeds.backend.product.category.Category;
import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.service.CategoryService;
import de.dogneeds.backend.service.ProductService;
import de.dogneeds.backend.service.UserService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Controller
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    UserService userService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    /**
     * loads all existing categories
     *
     * @return list of categories in json format
     */
    @CrossOrigin
    @RequestMapping("/get")
    public String getCategories() {
        return new JSONArray(categoryService.getCategories()).toString();
    }

    /**
     * gets products of a category
     *
     * @param categoryName the name of the category
     * @return list of the productIds seperated by ';'
     */
    @CrossOrigin
    @RequestMapping("/products")
    public String getProductsOfCategory(@RequestBody String categoryName) {
        Category category = categoryService.findCategoryByName(categoryName);
        if (category == null) {
            System.out.println("Couldn't find category " + categoryName);
            return "error;nocategory";
        }

        List<Product> products = productService.getProductsByCategory(category);
        if (products == null) {
            System.out.println("No products found in category " + category.getName());
            return "error;empty";
        }
        if (products.size() == 0) {
            System.out.println("No products found in category " + category.getName());
            return "error;empty";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < products.size(); i++) {
            stringBuilder.append(products.get(i).getPid());
            if (i != products.size() - 1) {
                stringBuilder.append(";");
            }
        }

        return stringBuilder.toString();
    }

    /**
     * handles requests to add a category
     *
     * @param details in json format containing session & name of the category
     * @return success if valid, error if invalid
     */
    @CrossOrigin
    @RequestMapping("/add")
    public String addCategory(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);
        String session = jsonObject.getString("session");
        String name = jsonObject.getString("categoryName");

        // the needed arguments are not given
        if (session == null || name == null) {
            return "error;wrongargs";
        }

        // permission check
        if (!userService.isAdmin(UUID.fromString(session))) {
            return "error;nopermission";
        }

        return categoryService.addCategory(name);
    }

    /**
     * handles requests to remove a category
     *
     * @param details session and the name of the category
     * @return success/error depending on if the action was successful
     */
    @CrossOrigin
    @RequestMapping("/remove")
    public String removeCategory(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);
        String session = String.valueOf(jsonObject.get("session"));
        String name = String.valueOf(jsonObject.get("categoryName"));

        // the needed arguments are not given
        if (session == null || name == null) {
            return "error;wrongargs";
        }

        // permission check
        if (!userService.isAdmin(UUID.fromString(session))) {
            return "error;nopermission";
        }

        return categoryService.removeCategory(name);
    }

    /**
     * handles requests to edit a category
     *
     * @param details session and the name of the category
     * @return success/error depending on if the action was successful
     */
    @CrossOrigin
    @RequestMapping("/edit")
    public String editCategory(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);
        String session = String.valueOf(jsonObject.get("session"));
        String name = String.valueOf(jsonObject.get("categoryName"));
        String newName = String.valueOf(jsonObject.get("newCategoryName"));

        // the needed arguments are not given
        if (session == null || name == null) {
            return "error;wrongargs";
        }

        // permission check
        if (!userService.isAdmin(UUID.fromString(session))) {
            return "error;nopermission";
        }

        return categoryService.editCategory(name, newName);
    }

}
