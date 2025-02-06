package notificationservice.dto;

import java.time.LocalDateTime;

public record ReplyResponse(String body, LocalDateTime timestamp, String emailStatus) {
}
