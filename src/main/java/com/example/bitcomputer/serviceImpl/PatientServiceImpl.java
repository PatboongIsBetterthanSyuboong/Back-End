package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.Repository.PatientRepository;
import com.example.bitcomputer.entity.Patient;
import com.example.bitcomputer.model.PatientDTO;
import com.example.bitcomputer.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PatientDTO createPatient(PatientDTO request) {
        validateRequest(request);

        if (patientRepository.existsById(request.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 환자입니다.");
        }

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setIdentityNumber(request.getIdentityNumber());
        patient.setBirth(convertToLocalDate(request.getBirth()));
        patient.setGender(request.getGender());

        Patient saved = patientRepository.save(patient);

        return mapToDto(saved);
    }

    @Override
    public PatientDTO searchPatientById(int id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id " + id));
        return mapToDto(patient);
    }

    private void validateRequest(PatientDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청 본문이 필요합니다.");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이름은 필수입니다.");
        }
        if (!StringUtils.hasText(request.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "연락처는 필수입니다.");
        }
        if (!StringUtils.hasText(request.getIdentityNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주민등록번호는 필수입니다.");
        }
        if (request.getBirth() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "생년월일은 필수입니다.");
        }
        if (!StringUtils.hasText(request.getGender())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "성별은 필수입니다.");
        }
    }

    private PatientDTO mapToDto(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setIdentityNumber(patient.getIdentityNumber());
        dto.setBirth(convertToDate(patient.getBirth()));
        dto.setGender(patient.getGender());
        return dto;
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date convertToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}

