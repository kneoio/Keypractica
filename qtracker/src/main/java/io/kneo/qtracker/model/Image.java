package io.kneo.qtracker.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {
    private byte[] imageData;  // For the image_data BYTEA column
    private String type;       // For the type TEXT column
    private double confidence; // For the confidence DOUBLE column
    private Map<String, Object> addInfo;  // For the add_info JSONB column
    private String description; // For the description TEXT column
}
