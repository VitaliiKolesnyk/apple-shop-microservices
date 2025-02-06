package productservice.dto.category;

import java.io.Serializable;
import java.util.List;

public record CategoryResponse(String id, String name, String thumbnailUrl, boolean isMain, List<CategoryResponse> subcategories) implements Serializable {
}
