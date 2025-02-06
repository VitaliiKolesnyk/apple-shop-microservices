package org.productservice.dto.category;

import org.springframework.web.multipart.MultipartFile;

public record CategoryRequest(String name, MultipartFile thumbnailFile) {
}
