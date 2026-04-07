package com.example.bitcomputer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * POST /api/agent/prescription/recommend 요청 본문.
 */
@Data
public class PrescriptionRecommendRequestDTO {

    /**
     * 조회할 환자 ID (현재 단계: 진료 기록 symptom / memo 수집용).
     */
    @JsonProperty("patient_id")
    private int patientId;

    // TODO(AI 처방 추천): 최종 스펙은 history_diagnose_id 단일 입력으로 상병·메모·증상 컨텍스트를 조합한다.
    // DB 및 HistoryDiagnose 연동 후 @JsonProperty("history_diagnose_id") Integer historyDiagnoseId 를 도입하고,
    // patient_id 와의 우선순위·검증 규칙을 정한다.
}
