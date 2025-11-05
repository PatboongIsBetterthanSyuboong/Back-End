package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.Repository.DiagnoseRepository;
import com.example.bitcomputer.entity.Diagnose;
import com.example.bitcomputer.model.DiagnoseDTO;
import com.example.bitcomputer.service.DiagnoseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiagnoseServiceImpl implements DiagnoseService {

    private final DiagnoseRepository diagnoseRepository;

    public DiagnoseServiceImpl(DiagnoseRepository diagnoseRepository) {
        this.diagnoseRepository = diagnoseRepository;
    }

    @Override
    public DiagnoseDTO getById(int id) {
        Diagnose entity = diagnoseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Diagnose not found with id " + id));
        return toDto(entity);
    }

    @Override
    public List<DiagnoseDTO> search(String query, String code, String name) {
        String codeQuery = (query != null) ? query : (code != null ? code : "");
        String nameQuery = (query != null) ? query : (name != null ? name : "");

        List<DiagnoseDTO> result = diagnoseRepository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(codeQuery, nameQuery)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return result;
    }

    private DiagnoseDTO toDto(Diagnose entity) {
        DiagnoseDTO dto = new DiagnoseDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDose(entity.getDose());
        dto.setTime(entity.getTime());
        dto.setDays(entity.getDays());
        return dto;
    }
}

