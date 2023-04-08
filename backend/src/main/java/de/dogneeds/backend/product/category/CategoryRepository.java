package de.dogneeds.backend.product.category;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    @Query("SELECT c FROM Category c")
    Collection<Category> getAllCategories();

    @Query("SELECT c FROM Category c WHERE UPPER(c.name) = UPPER(?1)")
    Category findCategoryByName(String name);

}
