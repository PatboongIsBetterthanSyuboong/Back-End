    package com.example.bitcomputer.controller;

import com.example.bitcomputer.model.PatientDTO;
import com.example.bitcomputer.model.HistoryDTO;
import com.example.bitcomputer.model.WriteHistoryDTO;
import com.example.bitcomputer.service.HistoryService;
import com.example.bitcomputer.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Map;
import java.util.Date;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final HistoryService historyService;

    public PatientController(PatientService patientService, HistoryService historyService) {
        this.patientService = patientService;
        this.historyService = historyService;
    }

    @PostMapping("/get_patient_id")
    public ResponseEntity<Map<String, Integer>> createPatient(@RequestBody PatientDTO request) {
        PatientDTO created = patientService.createPatient(request);
        Map<String, Integer> responseBody = Map.of("patientId", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @PostMapping("/search_history/{id}")
    public ResponseEntity<Map<String, Object>> searchHistory(@PathVariable("id") int employeeId, 
    @RequestParam("patientId") int patientId, 
    @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, 
    @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> response = historyService.searchHistory(patientId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search_patient/{id}")
    public ResponseEntity<PatientDTO> searchPatient(@PathVariable int id) {
        PatientDTO patient = patientService.searchPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @PostMapping("/update_history/{id}")
    public ResponseEntity<HistoryDTO> updateHistory(@PathVariable int id, @RequestBody WriteHistoryDTO request) {
        HistoryDTO history = historyService.updateHistory(id, request);
        return ResponseEntity.ok(history);
    }
}

