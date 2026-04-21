package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.Repository.HistoryDiagnoseRepository;
import com.example.bitcomputer.Repository.HistoryDiseaseRepository;
import com.example.bitcomputer.Repository.HistoryRepository;
import com.example.bitcomputer.Repository.PatientRepository;
import com.example.bitcomputer.entity.History;
import com.example.bitcomputer.entity.HistoryDiagnose;
import com.example.bitcomputer.entity.HistoryDisease;
import com.example.bitcomputer.entity.Patient;
import com.example.bitcomputer.model.HistoryDTO;
import com.example.bitcomputer.model.PrescriptionAgentRequest;
import com.example.bitcomputer.model.PrescriptionAgentResponse;
import com.example.bitcomputer.model.PrescriptionRecommendRequestDTO;
import com.example.bitcomputer.model.PrescriptionRecommendResponseDTO;
import com.example.bitcomputer.model.RecommendedPrescriptionItemDTO;
import com.example.bitcomputer.service.AgentService;
import com.example.bitcomputer.service.HistoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    private static final DateTimeFormatter ENTRY_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final HistoryService historyService;
    private final HistoryDiagnoseRepository historyDiagnoseRepository;
    private final HistoryRepository historyRepository;
    private final HistoryDiseaseRepository historyDiseaseRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionAgentClient prescriptionAgentClient;

    @Value("${ai.prescription-agent.fetch-top-rx-from-arango:true}")
    private boolean fetchTopRxFromArango;

    @Value("${ai.prescription-agent.arango-top-rx-limit:80}")
    private int arangoTopRxLimit;

    public AgentServiceImpl(
            HistoryService historyService,
            HistoryDiagnoseRepository historyDiagnoseRepository,
            HistoryRepository historyRepository,
            HistoryDiseaseRepository historyDiseaseRepository,
            PatientRepository patientRepository,
            PrescriptionAgentClient prescriptionAgentClient) {
        this.historyService = historyService;
        this.historyDiagnoseRepository = historyDiagnoseRepository;
        this.historyRepository = historyRepository;
        this.historyDiseaseRepository = historyDiseaseRepository;
        this.patientRepository = patientRepository;
        this.prescriptionAgentClient = prescriptionAgentClient;
    }

    @Override
    public PrescriptionRecommendResponseDTO recommendPrescription(PrescriptionRecommendRequestDTO request) {
        HistoryDiagnose hd = historyDiagnoseRepository.findById(request.getHistoryDiagnoseId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "HistoryDiagnose not found with id " + request.getHistoryDiagnoseId()));
        History currentHistory = historyRepository.findById(hd.getHistoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "History not found with id " + hd.getHistoryId()));
        Patient patient = patientRepository.findById(currentHistory.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Patient not found with id " + currentHistory.getPatientId()));

        Map<String, Object> historyBundle = historyService.searchHistory(
                patient.getId(), null, null);

        @SuppressWarnings("unchecked")
        List<HistoryDTO> histories = (List<HistoryDTO>) historyBundle.getOrDefault(
                "histories", Collections.emptyList());

        PrescriptionAgentRequest agentRequest = buildAgentRequest(patient, currentHistory, histories);

        List<RecommendedPrescriptionItemDTO> recommended = callAgentAndMap(agentRequest);

        return PrescriptionRecommendResponseDTO.builder()
                .historyDiagnoseId(request.getHistoryDiagnoseId())
                .recommendedPrescriptions(recommended)
                .build();
    }

    /**
     * MySQL 에서 모은 환자 feature 를 Python / ArangoDB 가 기대하는 스키마로 변환한다.
     *
     * <ul>
     *   <li>{@code patient_id} — Arango visits 의 {@code 내원번호_norm} 과 매칭되는 문자열.
     *       현재 스키마에서는 {@link Patient#getIdentityNumber()} 또는 fallback 으로 {@code patient.id}.</li>
     *   <li>{@code symptoms} — 현재 진료의 증상 기술(symptomDetail).</li>
     *   <li>{@code history} — 과거 진료들의 entry_date / symptom / memo 를 여러 줄로 합친 문자열.</li>
     *   <li>{@code top_rx} — 과거 History 에 적재된 HistoryDiagnose 를 Arango 스키마 키
     *       ({@code 내원번호, 처방시퀀스, 처방코드, 처방명}) 로 펼친다.</li>
     * </ul>
     */
    private PrescriptionAgentRequest buildAgentRequest(
            Patient patient, History current, List<HistoryDTO> histories) {

        String patientIdForGraph = resolvePatientIdForGraph(patient);

        List<Map<String, Object>> topRx = buildTopRx(histories);
        String historyText = buildHistoryText(current, histories);
        String symptoms = current.getSymptomDetail() != null ? current.getSymptomDetail() : "";

        return PrescriptionAgentRequest.builder()
                .patientId(patientIdForGraph)
                .symptoms(symptoms)
                .history(historyText)
                .topRx(topRx)
                .similarOutcomes("")
                .fetchTopRxFromArango(fetchTopRxFromArango)
                .arangoTopRxLimit(arangoTopRxLimit)
                .build();
    }

    private String resolvePatientIdForGraph(Patient patient) {
        // TODO(매핑): 실제 EMR 의 "내원번호" 컬럼이 추가되면 그 값을 우선 사용하도록 바꿀 것.
        //   지금은 identityNumber → patient.id 순으로 후보를 넘기고, Python 쪽에서
        //   내원번호_norm / visit_id / _key 중 어느 키로든 매칭되게 되어 있음.
        if (patient.getIdentityNumber() != null && !patient.getIdentityNumber().isBlank()) {
            return patient.getIdentityNumber().trim();
        }
        return String.valueOf(patient.getId());
    }

    private List<Map<String, Object>> buildTopRx(List<HistoryDTO> histories) {
        if (histories == null || histories.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> historyIds = histories.stream()
                .map(HistoryDTO::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (historyIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<HistoryDiagnose> diagnoses = historyDiagnoseRepository.findByHistoryIdIn(historyIds);
        List<Map<String, Object>> rows = new ArrayList<>(diagnoses.size());
        for (HistoryDiagnose d : diagnoses) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("내원번호", String.valueOf(d.getHistoryId()));
            row.put("처방시퀀스", String.valueOf(d.getId()));
            row.put("처방코드", d.getCode());
            row.put("처방명", d.getName());
            row.put("dose", d.getDose());
            row.put("time", d.getTime());
            row.put("days", d.getDays());
            rows.add(row);
        }
        return rows;
    }

    private String buildHistoryText(History current, List<HistoryDTO> histories) {
        StringBuilder sb = new StringBuilder();

        // 현재 진료 상병/질환 기록
        List<HistoryDisease> currentDiseases = historyDiseaseRepository.findByHistoryId(current.getId());
        if (!currentDiseases.isEmpty()) {
            sb.append("[현재 진료 상병] ");
            for (int i = 0; i < currentDiseases.size(); i++) {
                HistoryDisease d = currentDiseases.get(i);
                if (i > 0) sb.append(", ");
                sb.append(d.getCode()).append(":").append(d.getName());
                if (d.getDegree() != null && !d.getDegree().isBlank()) {
                    sb.append("(").append(d.getDegree()).append(")");
                }
            }
            sb.append('\n');
        }
        if (current.getMemo() != null && !current.getMemo().isBlank()) {
            sb.append("[현재 메모] ").append(current.getMemo()).append('\n');
        }

        // 과거 진료 요약
        if (histories != null) {
            int appended = 0;
            for (HistoryDTO h : histories) {
                if (h.getId() != null && h.getId() == current.getId()) {
                    continue; // 현재 진료는 위에서 이미 처리
                }
                String line = formatPastHistoryLine(h);
                if (line != null) {
                    sb.append(line).append('\n');
                    appended++;
                }
                if (appended >= 10) {
                    // 프롬프트 폭주 방지
                    break;
                }
            }
        }

        return sb.toString().trim();
    }

    private String formatPastHistoryLine(HistoryDTO h) {
        StringBuilder line = new StringBuilder("[과거 진료");
        if (h.getEntryDate() != null) {
            line.append(" ")
                    .append(ENTRY_FMT.format(
                            h.getEntryDate().toInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()));
        }
        line.append("] ");
        boolean hasContent = false;
        if (h.getSymptomDetail() != null && !h.getSymptomDetail().isBlank()) {
            line.append("증상=").append(h.getSymptomDetail().trim());
            hasContent = true;
        }
        if (h.getMemo() != null && !h.getMemo().isBlank()) {
            if (hasContent) line.append(" / ");
            line.append("메모=").append(h.getMemo().trim());
            hasContent = true;
        }
        return hasContent ? line.toString() : null;
    }

    private List<RecommendedPrescriptionItemDTO> callAgentAndMap(PrescriptionAgentRequest request) {
        Optional<PrescriptionAgentResponse> maybe = prescriptionAgentClient.recommend(request);
        if (maybe.isEmpty()) {
            log.info("처방 추천 에이전트 응답 없음 - 빈 recommended_prescriptions 반환");
            return Collections.emptyList();
        }
        PrescriptionAgentResponse resp = maybe.get();
        if (resp.getPrescriptions() == null || resp.getPrescriptions().isEmpty()) {
            return Collections.emptyList();
        }
        List<RecommendedPrescriptionItemDTO> out = new ArrayList<>(resp.getPrescriptions().size());
        for (PrescriptionAgentResponse.Item item : resp.getPrescriptions()) {
            out.add(RecommendedPrescriptionItemDTO.builder()
                    .rank(item.getRank())
                    .prescriptionCode(item.getPrescriptionCode())
                    .prescriptionName(item.getName())
                    .reason(item.getReason())
                    .confidenceScore(0.0) // Python 쪽 스키마엔 없음. 추후 top_rx 빈도 기반으로 채울 예정.
                    .build());
        }
        return out;
    }
}
