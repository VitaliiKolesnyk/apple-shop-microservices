package org.service.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.service.notificationservice.dto.LastReplyFrom;
import org.service.notificationservice.dto.Status;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "emails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailId;

    private String subject;
    private String body;
    private String email;
    private LocalDateTime timestamp;
    private String userId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private LastReplyFrom lastReplyFrom;

    private LocalDateTime lastReplyAt;

    @OneToMany(mappedBy = "email", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies;
}
