package inventoryservice.event;

import lombok.Data;

@Data
public class LimitExceedEvent {
    private String skuCode;
    private int limit;
}
