package com.example.bitcomputer.controller;

import com.example.bitcomputer.Repository.DiseaseRepository;
import com.example.bitcomputer.entity.Disease;
import com.example.bitcomputer.model.DiseaseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diseases")
public class DiseaseController {

    private final DiseaseRepository diseaseRepository;

    public DiseaseController(DiseaseRepository diseaseRepository) {
        this.diseaseRepository = diseaseRepository;
    }

    @GetMapping("/{code}")
    public ResponseEntity<DiseaseDTO> getByCode(@PathVariable String code) {
        return diseaseRepository.findByCode(code)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DiseaseDTO>> search(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name
    ) {
        String codeQuery = (query != null) ? query : (code != null ? code : "");
        String nameQuery = (query != null) ? query : (name != null ? name : "");

        List<DiseaseDTO> result = diseaseRepository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(codeQuery, nameQuery)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private DiseaseDTO toDto(Disease entity) {
        DiseaseDTO dto = new DiseaseDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        return dto;
    }
}