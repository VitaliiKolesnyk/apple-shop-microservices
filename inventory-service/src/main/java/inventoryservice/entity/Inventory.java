package inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "limit_qnty")
    private Integer limit;

    @Column(name = "limit_notification_sent")
    private boolean isLimitNotificationSent;

    @OneToOne(mappedBy = "inventory", cascade = CascadeType.ALL)
    private Product product;

    @Version
    private Integer version;
}
