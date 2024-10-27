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
    private byte[] imageData;
    private String type;
    private double confidence;
    private int numOfSeq;
    private Map<String, Object> addInfo;
    private String description;
}
