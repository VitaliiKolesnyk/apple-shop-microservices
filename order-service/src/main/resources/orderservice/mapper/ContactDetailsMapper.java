package org.service.orderservice.mapper;

import org.service.orderservice.dto.ContactDetailsRequest;
import org.service.orderservice.dto.ContactDetailsResponse;
import org.service.orderservice.entity.ContactDetails;

public interface ContactDetailsMapper {

    ContactDetails map(ContactDetailsRequest contactDetailsRequest);

    ContactDetailsResponse map(ContactDetails contactDetails);
}
