package productservice.repository;

import org.productservice.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    Optional<Category> findCategoryByName(String name);

    boolean existsCategoriesByName(String name);

    List<Category> findByParentCategoryId(String parentCategoryId);

    List<Category> findByParentCategoryIsNull();
}
