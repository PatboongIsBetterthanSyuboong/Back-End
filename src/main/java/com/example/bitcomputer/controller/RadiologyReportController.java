package com.example.bitcomputer.controller;

import com.example.bitcomputer.model.RadiologyReportRequestDTO;
import com.example.bitcomputer.model.RadiologyReportResponseDTO;
import com.example.bitcomputer.service.RadiologyReportService;
import com.example.bitcomputer.util.ImageStorageUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/radiology")
public class RadiologyReportController {

    private final RadiologyReportService radiologyReportService;
    private final ImageStorageUtil imageStorageUtil;

    public RadiologyReportController(
            RadiologyReportService radiologyReportService,
            ImageStorageUtil imageStorageUtil) {
        this.radiologyReportService = radiologyReportService;
        this.imageStorageUtil = imageStorageUtil;
    }

    @PostMapping("/report")
    public ResponseEntity<RadiologyReportResponseDTO> processRadiologyReport(
            @RequestBody RadiologyReportRequestDTO request) {
        try {
            RadiologyReportResponseDTO response = radiologyReportService.processRadiologyReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 이미지 업로드 및 AI 분석 통합 API
     * 
     * @param file 업로드된 이미지 파일
     * @param patientId 환자 ID
     * @param employeeId 근무자 ID
     * @param deptId 부서 ID
     * @param symptomDetail 증상 상세
     * @param memo 메모
     * @param entryDate 등록일자 (yyyy-MM-dd)
     * @return AI 분석 결과
     */
    @PostMapping(value = "/upload-and-analyze", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadAndAnalyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("patientId") int patientId,
            @RequestParam("employeeId") int employeeId,
            @RequestParam("deptId") int deptId,
            @RequestParam(value = "symptomDetail", required = false) String symptomDetail,
            @RequestParam(value = "memo", required = false) String memo,
            @RequestParam("entryDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate entryDate) {
        
        try {
            // 1. 이미지 파일 검증
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "이미지 파일이 필요합니다."));
            }
            
            // 파일 확장자 확인 (JPG, JPEG, PNG, DICOM만 허용)
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "파일명이 없습니다."));
            }
            
            String lowerFileName = fileName.toLowerCase();
            boolean isValidExtension = lowerFileName.endsWith(".jpg") ||
                    lowerFileName.endsWith(".jpeg") ||
                    lowerFileName.endsWith(".png") ||
                    lowerFileName.endsWith(".dcm") ||
                    lowerFileName.endsWith(".dicom");
            
            if (!isValidExtension) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "JPG, JPEG, PNG, DICOM 파일만 업로드 가능합니다."));
            }
            
            // 2. 이미지를 images/{patientId}/original/ 폴더에 저장
            String folderId = String.valueOf(patientId);
            String imageRelativePath = imageStorageUtil.saveImage(file, folderId, "original");
            
            // 3. RadiologyReportRequestDTO 생성
            RadiologyReportRequestDTO request = new RadiologyReportRequestDTO();
            request.setRadiologyRequestId(0); // DB에서 자동 생성
            request.setPatientId(patientId);
            request.setEmployeeId(employeeId);
            request.setDeptId(deptId);
            request.setSymptomDetail(symptomDetail);
            request.setMemo(memo);
            
            // LocalDate를 Date로 변환
            Date entryDateAsDate = java.sql.Date.valueOf(entryDate);
            request.setEntryDate(entryDateAsDate);
            request.setDetailImageAddress(imageRelativePath);
            
            // 4. AI 분석 요청 (Flask API 호출)
            RadiologyReportResponseDTO response = radiologyReportService.processRadiologyReport(request);
            
            // 5. 응답 반환 (오버레이 이미지 URL 포함)
            return ResponseEntity.ok(response);
            
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // ResponseStatusException은 메시지를 포함하여 반환
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason() != null ? e.getReason() : "처리 중 오류가 발생했습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "처리 중 오류가 발생했습니다.";
            if (e.getCause() != null) {
                errorMessage += " (" + e.getCause().getMessage() + ")";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }
}
