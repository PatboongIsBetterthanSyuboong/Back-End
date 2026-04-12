package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.Repository.HistoryDiagnoseRepository;
import com.example.bitcomputer.Repository.HistoryRepository;
import com.example.bitcomputer.entity.History;
import com.example.bitcomputer.entity.HistoryDiagnose;
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
    private final HistoryDiagnoseRepository historyDiagnoseRepository;
    private final HistoryRepository historyRepository;

    public AgentServiceImpl(
            HistoryService historyService,
            HistoryDiagnoseRepository historyDiagnoseRepository,
            HistoryRepository historyRepository) {
        this.historyService = historyService;
        this.historyDiagnoseRepository = historyDiagnoseRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public PrescriptionRecommendResponseDTO recommendPrescription(PrescriptionRecommendRequestDTO request) {
        HistoryDiagnose hd = historyDiagnoseRepository.findById(request.getHistoryDiagnoseId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "HistoryDiagnose not found with id " + request.getHistoryDiagnoseId()));
        History history = historyRepository.findById(hd.getHistoryId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "History not found with id " + hd.getHistoryId()));

        Map<String, Object> historyBundle = historyService.searchHistory(
                history.getPatientId(), null, null);

        @SuppressWarnings("unchecked")
        List<HistoryDTO> histories = (List<HistoryDTO>) historyBundle.get("histories");

        // TODO(AI): 외부 처방 추천 서비스 호출 — symptom_detail, memo, 상병·진단 등을 입력으로 상위 N개 처방 생성.
        // TODO(DB): 처방 마스터·매핑 조회 및 recommended_prescriptions 채움.

        return PrescriptionRecommendResponseDTO.builder()
                .historyDiagnoseId(request.getHistoryDiagnoseId())
                .patientId(history.getPatientId())
                .histories(histories != null ? histories : Collections.emptyList())
                .recommendedPrescriptions(Collections.emptyList())
                .build();
    }
}
