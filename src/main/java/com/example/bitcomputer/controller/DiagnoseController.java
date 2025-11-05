package com.example.bitcomputer.controller;

import com.example.bitcomputer.model.DiagnoseDTO;
import com.example.bitcomputer.service.DiagnoseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diagnoses")
public class DiagnoseController {

    private final DiagnoseService diagnoseService;

    public DiagnoseController(DiagnoseService diagnoseService) {
        this.diagnoseService = diagnoseService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiagnoseDTO> getById(@PathVariable int id) {
        try {
            DiagnoseDTO dto = diagnoseService.getById(id);
            return ResponseEntity.ok(dto);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<DiagnoseDTO>> search(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name
    ) {
        List<DiagnoseDTO> result = diagnoseService.search(query, code, name);
        return ResponseEntity.ok(result);
    }
}

