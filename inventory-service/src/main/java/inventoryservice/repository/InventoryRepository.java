package inventoryservice.repository;

import org.service.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductSkuCode(String skuCode);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.limit AND i.isLimitNotificationSent = false")
    List<Inventory> findInventoriesExceedingLimit();


}
