package com.example.bitcomputer.controller;

import com.example.bitcomputer.Repository.HistoryRepository;
import com.example.bitcomputer.Repository.ValidationResultRepository;
import com.example.bitcomputer.entity.History;
import com.example.bitcomputer.entity.ValidationEvent;
import com.example.bitcomputer.entity.ValidationResult;
import com.example.bitcomputer.model.ValidationResultDTO;
import com.example.bitcomputer.serviceImpl.ValidationEventProcessor;
import com.example.bitcomputer.serviceImpl.ValidationOutboxService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/histories")
public class ValidationResultController {

    private final HistoryRepository historyRepository;
    private final ValidationResultRepository validationResultRepository;
    private final ValidationOutboxService validationOutboxService;
    private final ValidationEventProcessor validationEventProcessor;

    public ValidationResultController(
            HistoryRepository historyRepository,
            ValidationResultRepository validationResultRepository,
            ValidationOutboxService validationOutboxService,
            ValidationEventProcessor validationEventProcessor) {
        this.historyRepository = historyRepository;
        this.validationResultRepository = validationResultRepository;
        this.validationOutboxService = validationOutboxService;
        this.validationEventProcessor = validationEventProcessor;
    }

    @GetMapping("/{historyId}/validation_results")
    public ResponseEntity<List<ValidationResultDTO>> getValidationResults(
            @PathVariable int historyId,
            @RequestParam("employeeId") int employeeId) {
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new EntityNotFoundException("History not found with id " + historyId));
        if (history.getEmployeeId() != employeeId) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }

        List<ValidationResultDTO> results = validationResultRepository
                .findByHistoryIdOrderByCreatedAtDesc(historyId)
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{historyId}/validation_results/run")
    public ResponseEntity<List<ValidationResultDTO>> runValidationAgent(
            @PathVariable int historyId,
            @RequestParam("employeeId") int employeeId) {
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new EntityNotFoundException("History not found with id " + historyId));
        if (history.getEmployeeId() != employeeId) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }

        ValidationEvent event = validationOutboxService.enqueueHistoryValidation(
                "MANUAL_VALIDATION_REQUESTED",
                history,
                Map.of("trigger", "button"));
        try {
            validationEventProcessor.process(event);
            validationOutboxService.markDone(event);
        } catch (RuntimeException e) {
            validationOutboxService.markFailed(event, e.getMessage(), 1);
            throw e;
        }

        List<ValidationResultDTO> results = validationResultRepository
                .findByHistoryIdOrderByCreatedAtDesc(historyId)
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(results);
    }

    private ValidationResultDTO toDto(ValidationResult result) {
        return ValidationResultDTO.builder()
                .id(result.getId())
                .eventId(result.getEventId())
                .historyId(result.getHistoryId())
                .overallStatus(result.getOverallStatus())
                .summary(result.getSummary())
                .resultJson(result.getResultJson())
                .shouldNotifyDoctor(result.isShouldNotifyDoctor())
                .shouldBlockAutoPrescription(result.isShouldBlockAutoPrescription())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
