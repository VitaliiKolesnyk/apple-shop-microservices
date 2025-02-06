package org.service.notificationservice.mapper;

import org.service.notificationservice.dto.EmailResponse;
import org.service.notificationservice.dto.EmailResponseWithReplies;
import org.service.notificationservice.entity.Email;

import java.util.List;

public interface EmailMapper {

    EmailResponse mapToResponse(Email email);

    List<EmailResponse> map(List<Email> emails);

    EmailResponseWithReplies map(Email email);
}
