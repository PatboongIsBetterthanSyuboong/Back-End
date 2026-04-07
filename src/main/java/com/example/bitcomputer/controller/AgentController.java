package com.example.bitcomputer.controller;

import com.example.bitcomputer.model.PrescriptionRecommendRequestDTO;
import com.example.bitcomputer.model.PrescriptionRecommendResponseDTO;
import com.example.bitcomputer.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 에이전트(처방 추천 등) API.
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 환자 컨텍스트를 바탕으로 처방 후보를 추천한다.
     * <p>
     * 현재: {@link com.example.bitcomputer.service.HistoryService#searchHistory(int, java.util.Date, java.util.Date)} 로
     * 해당 환자의 진료 기록(symptom_detail, memo 등)을 모두 조회한다.
     * </p>
     */
    @PostMapping("/prescription/recommend")
    public ResponseEntity<PrescriptionRecommendResponseDTO> recommendPrescription(
            @RequestBody PrescriptionRecommendRequestDTO request) {
        return ResponseEntity.ok(agentService.recommendPrescription(request));
    }
}
