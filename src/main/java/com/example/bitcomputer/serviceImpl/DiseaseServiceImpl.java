package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.Repository.DiseaseRepository;
import com.example.bitcomputer.entity.Disease;
import com.example.bitcomputer.model.DiseaseDTO;
import com.example.bitcomputer.service.DiseaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiseaseServiceImpl implements DiseaseService {

    private final DiseaseRepository diseaseRepository;

    public DiseaseServiceImpl(DiseaseRepository diseaseRepository) {
        this.diseaseRepository = diseaseRepository;
    }

    @Override
    public DiseaseDTO getById(int id) {
        Disease entity = diseaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Disease not found with id " + id));
        return toDto(entity);
    }

    @Override
    public List<DiseaseDTO> search(String query, String code, String name) {
        String codeQuery = (query != null) ? query : (code != null ? code : "");
        String nameQuery = (query != null) ? query : (name != null ? name : "");

        List<DiseaseDTO> result = diseaseRepository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(codeQuery, nameQuery)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return result;
    }

    private DiseaseDTO toDto(Disease entity) {
        DiseaseDTO dto = new DiseaseDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        return dto;
    }
}

