package org.service.notificationservice.service;

import org.service.notificationservice.dto.ContactRequest;
import org.service.notificationservice.dto.EmailResponse;
import org.service.notificationservice.dto.EmailResponseWithReplies;
import org.service.notificationservice.dto.EmailUpdateRequest;

import java.util.List;

public interface ContactService {

   EmailResponse saveContactMessage(ContactRequest contactRequest);

   List<EmailResponse> getEmails(String userId);

   EmailResponseWithReplies getEmail(Long id);

   void reply(Long emailId, ContactRequest contactRequest);

   EmailResponse update(Long id, EmailUpdateRequest emailUpdateRequest);

   void delete(Long id);
}
