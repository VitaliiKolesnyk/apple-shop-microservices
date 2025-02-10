package productservice.repository;

import org.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategoriesIn(List<String> categories, Pageable pageable);

    List<Product> findByCategoriesIn(List<String> categories);

    Page<Product> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsProductsBySkuCode(String skuCode);
}
