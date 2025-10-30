    package com.example.bitcomputer.controller;

import com.example.bitcomputer.model.PatientDTO;
import com.example.bitcomputer.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/get_patient_id")
    public ResponseEntity<Map<String, Integer>> createPatient(@RequestBody PatientDTO request) {
        PatientDTO created = patientService.createPatient(request);
        Map<String, Integer> responseBody = Map.of("patientId", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }
}

