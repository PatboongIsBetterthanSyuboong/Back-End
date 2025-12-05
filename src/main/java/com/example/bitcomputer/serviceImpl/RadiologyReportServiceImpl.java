package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.Repository.RadiologyReportRepository;
import com.example.bitcomputer.entity.RadiologyReport;
import com.example.bitcomputer.model.RadiologyReportRequestDTO;
import com.example.bitcomputer.model.RadiologyReportResponseDTO;
import com.example.bitcomputer.service.RadiologyReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class RadiologyReportServiceImpl implements RadiologyReportService {

    private final RadiologyReportRepository radiologyReportRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.api.base-url:http://localhost:5000}")
    private String aiApiBaseUrl;

    public RadiologyReportServiceImpl(
            RadiologyReportRepository radiologyReportRepository,
            RestTemplate restTemplate) {
        this.radiologyReportRepository = radiologyReportRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public RadiologyReportResponseDTO processRadiologyReport(RadiologyReportRequestDTO request) {
        try {
            // Flask API로 요청 전송
            String url = aiApiBaseUrl + "/api/ai/radiology_report";
            
            log.info("Flask API 호출 시작 - URL: {}, 이미지 경로: {}", url, request.getDetailImageAddress());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<RadiologyReportRequestDTO> httpEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<RadiologyReportResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    RadiologyReportResponseDTO.class
            );
            
            log.info("Flask API 응답 받음 - Status: {}", response.getStatusCode());
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "AI API에서 응답을 받지 못했습니다."
                );
            }
            
            RadiologyReportResponseDTO responseDTO = response.getBody();
            
            log.info("Flask API 응답 데이터 - result: {}, imageUrl: {}, summary: {}, status: {}", 
                    responseDTO != null ? responseDTO.isResult() : "null",
                    responseDTO != null ? responseDTO.getImageUrl() : "null",
                    responseDTO != null ? responseDTO.getSummary() : "null",
                    responseDTO != null ? responseDTO.getStatus() : "null");
            
            // 데이터베이스에 저장
            RadiologyReport report = new RadiologyReport();
            // radiologyRequestId는 @GeneratedValue로 자동 생성되므로 설정하지 않음
            report.setPatientId(request.getPatientId());
            report.setEmployeeId(request.getEmployeeId());
            report.setDeptId(request.getDeptId());
            report.setSymptomDetail(request.getSymptomDetail());
            report.setMemo(request.getMemo());
            report.setEntryDate(convertToLocalDate(request.getEntryDate()));
            report.setDetailImageAddress(request.getDetailImageAddress());
            report.setResult(responseDTO != null ? responseDTO.isResult() : false);
            report.setSummary(responseDTO != null ? responseDTO.getSummary() : null);
            report.setImageUrl(responseDTO != null ? responseDTO.getImageUrl() : null);
            report.setStatus(responseDTO != null ? responseDTO.getStatus() : null);
            
            log.info("저장할 데이터 - patientId: {}, employeeId: {}, deptId: {}, entryDate: {}, detailImageAddress: {}, result: {}", 
                    report.getPatientId(), report.getEmployeeId(), report.getDeptId(), 
                    report.getEntryDate(), report.getDetailImageAddress(), report.getResult());
            
            radiologyReportRepository.save(report);
            
            return responseDTO;
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // HTTP 4xx 오류 (클라이언트 오류)
            String errorBody = e.getResponseBodyAsString();
            log.error("Flask API 클라이언트 오류 (HTTP {}): {}", e.getStatusCode(), errorBody, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI API에서 오류가 발생했습니다 (HTTP " + e.getStatusCode() + "): " + 
                    (errorBody != null && !errorBody.isEmpty() ? errorBody : e.getMessage())
            );
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // HTTP 5xx 오류 (서버 오류)
            String errorBody = e.getResponseBodyAsString();
            log.error("Flask API 서버 오류 (HTTP {}): {}", e.getStatusCode(), errorBody, e);
            
            // JSON 응답에서 error 필드 추출 시도
            String errorMessage = "AI API 서버 오류";
            if (errorBody != null && !errorBody.isEmpty()) {
                try {
                    // 간단한 JSON 파싱 (error 필드 추출)
                    if (errorBody.contains("\"error\"")) {
                        int errorStart = errorBody.indexOf("\"error\"") + 8;
                        int errorEnd = errorBody.indexOf("\"", errorStart);
                        if (errorEnd > errorStart) {
                            errorMessage = errorBody.substring(errorStart, errorEnd);
                        } else {
                            errorMessage = errorBody;
                        }
                    } else {
                        errorMessage = errorBody;
                    }
                } catch (Exception parseEx) {
                    errorMessage = errorBody;
                }
            }
            
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI API 서버 오류 (HTTP " + e.getStatusCode() + "): " + errorMessage
            );
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 연결 오류 (Flask 서버가 실행되지 않음)
            log.error("Flask API 서버 연결 실패: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI API 서버에 연결할 수 없습니다. Flask 서버가 실행 중인지 확인하세요: " + e.getMessage()
            );
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Flask API 통신 오류: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI API와 통신 중 오류가 발생했습니다: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("영상 판독 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            String errorMsg = "영상 판독 처리 중 오류가 발생했습니다: " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += " (" + e.getCause().getMessage() + ")";
            }
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    errorMsg
            );
        }
    }

    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        // java.sql.Date는 toInstant()를 지원하지 않으므로 직접 변환
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        // java.util.Date인 경우
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
