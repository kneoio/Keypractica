package io.kneo.qtracker.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDTO {
    private String imageData;
    private String type;
    private double confidence;
    private int numOfSeq;
    private Map<String, Object> addInfo;
    private String description;
}
