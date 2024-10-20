package io.kneo.qtracker.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDTO {
    private String imageData;  // For the image_data BYTEA column
    private String type;       // For the type TEXT column
    private double confidence; // For the confidence DOUBLE column
    private Map<String, Object> addInfo;  // For the add_info JSONB column
    private String description; // For the description TEXT column
}
