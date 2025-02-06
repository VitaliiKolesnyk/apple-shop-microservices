package org.service.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import org.service.notificationservice.dto.ContactRequest;
import org.service.notificationservice.dto.EmailResponse;
import org.service.notificationservice.dto.EmailResponseWithReplies;
import org.service.notificationservice.dto.EmailUpdateRequest;
import org.service.notificationservice.service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contactMessage")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmailResponse getContactMessage(@RequestBody ContactRequest contactRequest) {
        return contactService.saveContactMessage(contactRequest);
    }

    @GetMapping
    public List<EmailResponse> findAllEmails(@RequestParam(required = false) String userId) {
        return contactService.getEmails(userId);
    }

    @GetMapping("/{emailId}")
    public EmailResponseWithReplies findEmailById(@PathVariable Long emailId) {
        return contactService.getEmail(emailId);
    }

    @PostMapping("/{emailId}/reply")
    public void reply(@PathVariable Long emailId, @RequestBody ContactRequest contactRequest) {
        contactService.reply(emailId, contactRequest);
    }

    @PutMapping("/{emailId}")
    public EmailResponse update(@PathVariable Long emailId, @RequestBody EmailUpdateRequest emailUpdateRequest) {
        return contactService.update(emailId, emailUpdateRequest);
    }

    @DeleteMapping("/{emailId}")
    public void delete(@PathVariable Long emailId) {
        contactService.delete(emailId);
    }
}
