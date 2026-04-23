package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.Repository.HistoryDiagnoseRepository;
import com.example.bitcomputer.Repository.HistoryDiseaseRepository;
import com.example.bitcomputer.Repository.HistoryRepository;
import com.example.bitcomputer.Repository.PatientRepository;
import com.example.bitcomputer.Repository.DiagnoseRepository;
import com.example.bitcomputer.entity.Diagnose;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
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
    private final DiagnoseRepository diagnoseRepository;
    private final PrescriptionAgentClient prescriptionAgentClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.prescription-agent.fetch-top-rx-from-arango:true}")
    private boolean fetchTopRxFromArango;

    @Value("${ai.prescription-agent.arango-top-rx-limit:80}")
    private int arangoTopRxLimit;

    @Value("${ai.prescription-agent.example-context-path:../GraphDB/langchain_graph_qa/patient_ctx.example.json}")
    private String exampleContextPath;

    public AgentServiceImpl(
            HistoryService historyService,
            HistoryDiagnoseRepository historyDiagnoseRepository,
            HistoryRepository historyRepository,
            HistoryDiseaseRepository historyDiseaseRepository,
            PatientRepository patientRepository,
            DiagnoseRepository diagnoseRepository,
            ObjectMapper objectMapper,
            PrescriptionAgentClient prescriptionAgentClient) {
        this.historyService = historyService;
        this.historyDiagnoseRepository = historyDiagnoseRepository;
        this.historyRepository = historyRepository;
        this.historyDiseaseRepository = historyDiseaseRepository;
        this.patientRepository = patientRepository;
        this.diagnoseRepository = diagnoseRepository;
        this.objectMapper = objectMapper;
        this.prescriptionAgentClient = prescriptionAgentClient;
    }

    @Override
    public PrescriptionRecommendResponseDTO recommendPrescription(PrescriptionRecommendRequestDTO request) {
        History currentHistory = resolveCurrentHistory(request);
        Patient patient = patientRepository.findById(currentHistory.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Patient not found with id " + currentHistory.getPatientId()));

        Map<String, Object> historyBundle = historyService.searchHistory(
                patient.getId(), null, null);

        @SuppressWarnings("unchecked")
        List<HistoryDTO> histories = (List<HistoryDTO>) historyBundle.getOrDefault(
                "histories", Collections.emptyList());

        PrescriptionAgentRequest agentRequest = buildAgentRequest(
                patient,
                currentHistory,
                histories,
                request.getArangoPatientId());
        applyExampleContextIfRequested(agentRequest, request);

        List<RecommendedPrescriptionItemDTO> recommended = callAgentAndMap(agentRequest);

        return PrescriptionRecommendResponseDTO.builder()
                .historyDiagnoseId(request.getHistoryDiagnoseId())
                .recommendedPrescriptions(recommended)
                .build();
    }

    @SuppressWarnings("unchecked")
    private void applyExampleContextIfRequested(
            PrescriptionAgentRequest agentRequest,
            PrescriptionRecommendRequestDTO request) {
        if (!Boolean.TRUE.equals(request.getUseExampleContext())) {
            return;
        }
        try {
            Path path = Path.of(exampleContextPath);
            if (!path.isAbsolute()) {
                path = Path.of("").toAbsolutePath().resolve(path).normalize();
            }
            if (!Files.exists(path)) {
                log.warn("example context 파일이 없어 기본 컨텍스트를 사용합니다: {}", path);
                return;
            }

            Map<String, Object> ctx = objectMapper.readValue(path.toFile(), Map.class);
            Object patientId = ctx.get("patient_id");
            if (patientId != null) {
                agentRequest.setPatientId(String.valueOf(patientId));
            }
            if (ctx.get("symptoms") != null) {
                agentRequest.setSymptoms(String.valueOf(ctx.get("symptoms")));
            }
            if (ctx.get("history") != null) {
                agentRequest.setHistory(String.valueOf(ctx.get("history")));
            }
            if (ctx.get("similar_outcomes") != null) {
                agentRequest.setSimilarOutcomes(String.valueOf(ctx.get("similar_outcomes")));
            }
            Object topRx = ctx.get("top_rx");
            if (topRx instanceof List<?> topRxList) {
                agentRequest.setTopRx((List<Map<String, Object>>) topRxList);
            }
            Object mentionLinks = ctx.get("mention_links");
            if (mentionLinks instanceof List<?> mentionList) {
                agentRequest.setMentionLinks((List<Map<String, Object>>) mentionList);
            }
            log.info("AI 추천에 example context 적용: {}", path);
        } catch (Exception e) {
            log.warn("example context 적용 실패, 기본 컨텍스트 사용: {}", e.getMessage());
        }
    }

    private History resolveCurrentHistory(PrescriptionRecommendRequestDTO request) {
        if (request.getHistoryId() != null) {
            return historyRepository.findById(request.getHistoryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "History not found with id " + request.getHistoryId()));
        }
        if (request.getHistoryDiagnoseId() != null) {
            HistoryDiagnose hd = historyDiagnoseRepository.findById(request.getHistoryDiagnoseId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "HistoryDiagnose not found with id " + request.getHistoryDiagnoseId()));
            return historyRepository.findById(hd.getHistoryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "History not found with id " + hd.getHistoryId()));
        }
        throw new IllegalArgumentException("history_id 또는 history_diagnose_id 중 하나는 필수입니다.");
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
            Patient patient,
            History current,
            List<HistoryDTO> histories,
            String arangoPatientIdOverride) {

        String patientIdForGraph = resolvePatientIdForGraph(patient, arangoPatientIdOverride);

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

    private String resolvePatientIdForGraph(Patient patient, String override) {
        if (override != null && !override.isBlank()) {
            return override.trim();
        }
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
            Diagnose matched = findDiagnoseMaster(item);
            out.add(RecommendedPrescriptionItemDTO.builder()
                    .id(matched != null ? matched.getId() : 0)
                    .rank(item.getRank())
                    .prescriptionCode(item.getPrescriptionCode())
                    .prescriptionName(item.getName())
                    .reason(item.getReason())
                    .confidenceScore(0.0) // Python 쪽 스키마엔 없음. 추후 top_rx 빈도 기반으로 채울 예정.
                    .dose(matched != null ? matched.getDose() : 0)
                    .time(matched != null ? matched.getTime() : 0)
                    .days(matched != null ? matched.getDays() : 0)
                    .build());
        }
        return out;
    }

    private Diagnose findDiagnoseMaster(PrescriptionAgentResponse.Item item) {
        if (item.getPrescriptionCode() != null && !item.getPrescriptionCode().isBlank()) {
            Optional<Diagnose> byCode = diagnoseRepository.findByCode(item.getPrescriptionCode().trim());
            if (byCode.isPresent()) {
                return byCode.get();
            }
        }
        if (item.getName() != null && !item.getName().isBlank()) {
            Optional<Diagnose> byName = diagnoseRepository.findByName(item.getName().trim());
            if (byName.isPresent()) {
                return byName.get();
            }
        }
        return createDiagnoseMasterFromAgentItem(item);
    }

    private Diagnose createDiagnoseMasterFromAgentItem(PrescriptionAgentResponse.Item item) {
        String code = normalizeText(item.getPrescriptionCode());
        String name = normalizeText(item.getName());

        if (code == null && name == null) {
            return null;
        }
        if (code == null) {
            code = "AUTO-" + Math.abs(name.hashCode());
        }
        if (name == null) {
            name = code;
        }

        try {
            Diagnose entity = new Diagnose();
            entity.setCode(code);
            entity.setName(name);
            entity.setDose(0);
            entity.setTime(0);
            entity.setDays(0);
            Diagnose saved = diagnoseRepository.save(entity);
            log.info("AI 추천 처방을 diagnose 마스터에 자동 등록 - id={} code={} name={}", saved.getId(), code, name);
            return saved;
        } catch (Exception e) {
            log.warn("AI 추천 처방 diagnose 자동 등록 실패 - code={} name={} err={}", code, name, e.getMessage());
            Optional<Diagnose> byCode = diagnoseRepository.findByCode(code);
            if (byCode.isPresent()) {
                return byCode.get();
            }
            return diagnoseRepository.findByName(name).orElse(null);
        }
    }

    private String normalizeText(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        if (t.isEmpty() || "미기재".equals(t)) {
            return null;
        }
        return t;
    }
}
