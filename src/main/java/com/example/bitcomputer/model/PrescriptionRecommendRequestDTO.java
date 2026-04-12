package com.example.bitcomputer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * POST /api/agent/prescription/recommend 요청 본문.
 * {@code history_diagnose_id}로 연결된 진료(history)의 환자 진료 기록을 조회한다.
 */
@Data
public class PrescriptionRecommendRequestDTO {

    @JsonProperty("history_diagnose_id")
    private int historyDiagnoseId;
}
