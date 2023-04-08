package de.dogneeds.backend.service;

import de.dogneeds.backend.product.category.Category;
import de.dogneeds.backend.product.category.CategoryRepository;
import de.dogneeds.backend.product.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    ProductService productService;

    /**
     * get a list of all categories
     *
     * @return collection of categories
     */
    public Collection<Category> getCategories() {
        return categoryRepository.getAllCategories();
    }

    /**
     * finds a category by its name
     *
     * @param categoryName the name of the category
     * @return object of the category or null if not existing
     */
    public Category findCategoryByName(String categoryName) {
        return categoryRepository.findCategoryByName(categoryName);
    }

    /**
     * adds a category
     *
     * @param categoryName the name of the category which should be added
     * @return result of the request (success/error)
     */
    public String addCategory(String categoryName) {
        Category repoCategory = categoryRepository.findCategoryByName(categoryName);
        if (repoCategory != null) {
            return "error;exists";
        }
        System.out.println("Added category " + categoryName);
        categoryRepository.save(new Category(categoryName));
        return "success";
    }

    /**
     * removes a category
     *
     * @param categoryName the name of the category which should be removed
     * @return result of the request (success/error)
     */
    public String removeCategory(String categoryName) {
        Category repoCategory = categoryRepository.findCategoryByName(categoryName);
        if (repoCategory == null) {
            return "error;notfound";
        }

        for (Product p : productService.getProductsByCategory(repoCategory)) {
            p.setCid(null);
            productService.getProductRepository().save(p);
        }

        System.out.println("Deleted category " + categoryName);
        categoryRepository.deleteById(repoCategory.getCid());
        return "success";
    }

    /**
     * edits a category
     *
     * @param categoryName    the old category name
     * @param newCategoryName the new category name
     * @return success/error depending on if the action was successful
     */
    public String editCategory(String categoryName, String newCategoryName) {
        Category repoCategory = categoryRepository.findCategoryByName(categoryName);
        if (repoCategory == null) {
            return "error;notfound";
        }
        System.out.println("Renamed category " + categoryName + " to " + newCategoryName);
        repoCategory.setName(newCategoryName);
        categoryRepository.save(repoCategory);
        return "success";
    }

}
