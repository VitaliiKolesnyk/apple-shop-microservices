package notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.service.notificationservice.dto.EmailStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "replies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long replyId;

    @ManyToOne
    @JoinColumn(name = "email_id")
    private Email email;

    private String subject;
    private String body;
    private String fromEmail;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private EmailStatus emailStatus;
}
