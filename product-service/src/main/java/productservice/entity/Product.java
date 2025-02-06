package productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Document(value = "product")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Product {

    @Id
    private String id;

    private String name;

    private String skuCode;

    private String description;

    private Double price;

    private String thumbnailUrl;

    private List<String> categories;

    private boolean isExclusive;

    @DBRef
    private List<Comment> comments = new ArrayList<>();
}
