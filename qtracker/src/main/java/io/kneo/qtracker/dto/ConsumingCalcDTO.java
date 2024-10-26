package io.kneo.qtracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumingCalcDTO {
    private double totalTrip;
    private double litersPerHundred;
}
