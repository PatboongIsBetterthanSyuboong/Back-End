package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.model.HistoryDTO;
import com.example.bitcomputer.model.PrescriptionRecommendRequestDTO;
import com.example.bitcomputer.model.PrescriptionRecommendResponseDTO;
import com.example.bitcomputer.service.AgentService;
import com.example.bitcomputer.service.HistoryService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AgentServiceImpl implements AgentService {

    private final HistoryService historyService;

    public AgentServiceImpl(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Override
    public PrescriptionRecommendResponseDTO recommendPrescription(PrescriptionRecommendRequestDTO request) {
        Map<String, Object> historyBundle = historyService.searchHistory(
                request.getPatientId(), null, null);

        @SuppressWarnings("unchecked")
        List<HistoryDTO> histories = (List<HistoryDTO>) historyBundle.get("histories");

        // TODO(AI): 외부 처방 추천 서비스 호출 — symptom_detail, memo, 상병(HistoryDisease), 진단(HistoryDiagnose) 등을 입력으로 상위 3개 처방 생성.
        // TODO(DB): 처방 마스터·매핑 테이블 조회 및 RecommendedPrescriptionItemDTO 채움.
        // TODO(인증): 에이전트 전용 grantType / accessToken / refreshToken 발급 정책이 확정되면 응답에 포함.
        // TODO(입력): history_diagnose_id 기반 단일 컨텍스트 조회로 전환 시 request DTO 및 조회 로직 교체.

        return PrescriptionRecommendResponseDTO.builder()
                .patientId(request.getPatientId())
                .histories(histories != null ? histories : Collections.emptyList())
                .recommendedPrescriptions(Collections.emptyList())
                .build();
    }
}
