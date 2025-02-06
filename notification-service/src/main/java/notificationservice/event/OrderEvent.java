package notificationservice.event;

import lombok.Data;

@Data
public class OrderEvent {
    String status;
    String orderNumber;
    String name;
    String email;
}
