package de.dogneeds.backend.service;

import de.dogneeds.backend.events.DeleteEvent;
import de.dogneeds.backend.product.category.Category;
import de.dogneeds.backend.product.category.CategoryRepository;
import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.product.product.ProductRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ProductService {

    private final HashMap<Long, byte[]> imageCache = new HashMap<>(); // stores pictures that have been already loaded once

    @Autowired
    FirebaseStorageService firebaseStorageService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * get a list of all products
     *
     * @return collection of products
     */
    public Collection<Product> getProducts() {
        return productRepository.getAllProducts();
    }

    /**
     * get a list of the best 3 products
     *
     * @return collection of products
     */
    public Collection<Product> getBestseller() {
        Collection<Product> products = getProducts();
        HashMap<Product, Integer> productSales = new HashMap<>();

        for (Product p : products) {
            Integer sales = productRepository.getSalesPerProduct(p);
            if (sales != null) {
                productSales.put(p, sales);
            } else {
                productSales.put(p, 0);
            }
        }

        ArrayList<Map.Entry<Product, Integer>> list = new ArrayList<>(productSales.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        LinkedHashMap<Product, Integer> sortedMap = new LinkedHashMap<>();
        // Put the sorted entries into the new LinkedHashMap
        for (Map.Entry<Product, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        Collection<Product> top3 = new ArrayList<>();

        int counter = 0;
        for (Map.Entry<Product, Integer> entry : sortedMap.entrySet()) {
            if (counter < 3) {
                counter++;
                top3.add(entry.getKey());
            }
        }

        return top3;
    }

    /**
     * get all products in a category
     *
     * @param category the given category
     * @return collection of products
     */
    public List<Product> getProductsByCategory(Category category) {
        Collection<Product> products = productRepository.getProductsByCid(category);
        if (products == null) {
            return null;
        }
        return products.stream().toList();
    }

    /**
     * request data of a product
     *
     * @param id      product-id
     * @param request string-array of the data that is requested
     * @return the data in the requested order, seperated by ';' (note that category will give the categoryId)
     */
    public String request(Long id, String[] request) {
        Product product = productRepository.getProductById(id);
        StringBuilder stringBuilder = new StringBuilder();
        if (product == null) {
            return "error;notfound";
        }
        for (int i = 0; i < request.length; i++) {
            switch (request[i]) {
                case "name" -> stringBuilder.append(product.getName());
                case "description" -> stringBuilder.append(product.getDescription());
                case "img" -> stringBuilder.append(product.getImg());
                case "price" -> stringBuilder.append(product.getPrice());
                case "category" -> stringBuilder.append(product.getCid() != null ? product.getCid().getName() : "null");
            }
            // only add a ';' if we're not at the end of the request
            if (i != request.length - 1) {
                stringBuilder.append(";");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * adds a new product
     *
     * @param jsonObject data of the product
     * @param image      the picture of the product, might be null!
     * @return success/error
     */
    public String addProduct(JSONObject jsonObject, MultipartFile image) {
        Category category = categoryRepository.findCategoryByName(jsonObject.getString("category"));
        if (category == null) {
            return "error;categorynotfound";
        }

        String fileName = null;
        try {
            if (image != null) {
                fileName = firebaseStorageService.save(image);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Product product = new Product(jsonObject.getString("name"), jsonObject.getString("description"), jsonObject.getDouble("price"), fileName, category);
        productRepository.save(product);
        if (image != null) {
            try {
                imageCache.put(product.getPid(), image.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Created product " + product.getName());
        return "success";
    }

    /**
     * edits a product
     *
     * @param jsonObject contains all the changed values
     * @param image      the picture of the product, might be null!
     * @return success/error
     */
    public String editProduct(JSONObject jsonObject, MultipartFile image) {
        Long productId = jsonObject.getLong("productId");
        Product product = productRepository.getProductById(productId);
        System.out.print("Changing:");
        if (product == null) {
            return "error;notfound";
        }
        // check for changes
        if (jsonObject.has("name")) {
            product.setName(jsonObject.getString("name"));
            System.out.print(" name");
        }
        if (jsonObject.has("description")) {
            product.setDescription(jsonObject.getString("description"));
            System.out.println(" description");
        }
        if (jsonObject.has("price")) {
            product.setPrice(jsonObject.getDouble("price"));
            System.out.print(" price");
        }
        if (jsonObject.has("category")) {
            Category category = categoryRepository.findCategoryByName(jsonObject.getString("category"));
            product.setCid(category);
            System.out.print(" category");
        }

        System.out.print(" of product " + productId + "\n");

        try {
            if (image != null) {
                if (product.getImg() != null) {
                    firebaseStorageService.delete(product.getImg());
                    imageCache.remove(productId);
                }
                String fileName = firebaseStorageService.save(image);
                product.setImg(fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // save the changed product
        productRepository.save(product);
        if (image != null) {
            try {
                imageCache.put(product.getPid(), image.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return "success";
    }

    /**
     * deletes a product
     *
     * @param productId the id of the product which should be deleted
     * @return success / error
     */
    public String deleteProduct(Long productId) {
        Product product = productRepository.getProductById(productId);
        if (product == null) {
            return "error;notfound";
        }

        if (product.getImg() != null) {
            System.out.println("Delete picture of product " + product.getName() + ": " + firebaseStorageService.delete(product.getImg()));
            imageCache.remove(productId);
        }

        applicationEventPublisher.publishEvent(new DeleteEvent(this, product));

        productRepository.delete(product);
        System.out.println("Deleted product " + product.getName());
        return "success";
    }

    /**
     * get an image
     *
     * @param productId the id of the product
     * @return byte array or null
     */
    public byte[] getImage(Long productId) {
        Product product = productRepository.getProductById(productId);
        if (product == null) {
            return null;
        }
        if (product.getImg() == null) {
            return null;
        }

        if (!imageCache.containsKey(productId)) {
            byte[] img = firebaseStorageService.load(product.getImg());
            imageCache.put(productId, img);
        }

        return imageCache.get(productId);
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }
}
