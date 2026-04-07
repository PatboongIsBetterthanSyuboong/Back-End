package com.example.bitcomputer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 처방 추천 1건 (향후 AI 응답 스키마와 동일하게 유지).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedPrescriptionItemDTO {

    private int rank;

    @JsonProperty("prescription_code")
    private String prescriptionCode;

    @JsonProperty("prescription_name")
    private String prescriptionName;

    private String reason;

    @JsonProperty("confidence_score")
    private double confidenceScore;
}
