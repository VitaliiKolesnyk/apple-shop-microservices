package org.service.orderservice.mapper.impl;

import org.service.orderservice.dto.ContactDetailsRequest;
import org.service.orderservice.dto.ContactDetailsResponse;
import org.service.orderservice.entity.ContactDetails;
import org.service.orderservice.mapper.ContactDetailsMapper;
import org.springframework.stereotype.Component;

@Component
public class ContactDetailsMapperImpl implements ContactDetailsMapper {
    @Override
    public ContactDetails map(ContactDetailsRequest contactDetailsRequest) {
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setName(contactDetailsRequest.name());
        contactDetails.setSurname(contactDetailsRequest.surname());
        contactDetails.setEmail(contactDetailsRequest.email());
        contactDetails.setPhone(contactDetailsRequest.phone());
        contactDetails.setCountry(contactDetailsRequest.country());
        contactDetails.setCity(contactDetailsRequest.city());
        contactDetails.setStreet(contactDetailsRequest.street());

        return contactDetails;
    }

    @Override
    public ContactDetailsResponse map(ContactDetails contactDetails) {
        return new ContactDetailsResponse(contactDetails.getName(), contactDetails.getSurname(), contactDetails.getEmail(),
                contactDetails.getPhone(), contactDetails.getCountry(), contactDetails.getCity(), contactDetails.getStreet());
    }
}
