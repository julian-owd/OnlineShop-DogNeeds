package de.dogneeds.backend.product.product;


import de.dogneeds.backend.product.category.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface ProductRepository extends CrudRepository<Product, Long> {
    @Query("SELECT p FROM Product p")
    Collection<Product> getAllProducts();

    @Query("SELECT SUM(op.amount) FROM Product p JOIN OrderingPos op ON op.pid = ?1 WHERE p = ?1 GROUP BY op.pid")
    Integer getSalesPerProduct(Product p);

    @Query("SELECT p FROM Product p WHERE p.pid=?1")
    Product getProductById(Long pid);

    @Query("SELECT p FROM Product p WHERE p.cid=?1")
    Collection<Product> getProductsByCid(Category category);

}
