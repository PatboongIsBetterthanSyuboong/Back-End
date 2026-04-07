package com.example.bitcomputer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * POST /api/agent/prescription/recommend 응답.
 * 토큰·추천 목록은 추후 연동; null/빈 값은 직렬화에서 제외할 수 있음.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrescriptionRecommendResponseDTO {

    private String grantType;

    private String accessToken;

    private String refreshToken;

    @JsonProperty("history_diagnose_id")
    private Integer historyDiagnoseId;

    @JsonProperty("patient_id")
    private Integer patientId;

    /**
     * 환자 진료 기록(증상 상세, 메모 등) — 현재는 HistoryService 로 조회.
     */
    private List<HistoryDTO> histories;

    @JsonProperty("recommended_prescriptions")
    private List<RecommendedPrescriptionItemDTO> recommendedPrescriptions;
}
