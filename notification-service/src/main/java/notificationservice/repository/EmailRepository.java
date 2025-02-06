package notificationservice.repository;

import org.service.notificationservice.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

    Optional<Email> findByEmailAndSubject(String email, String subject);

    List<Email> findAllByUserId(String userId);
}
