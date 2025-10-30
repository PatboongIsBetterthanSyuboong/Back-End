package com.example.bitcomputer.controller;

import com.example.bitcomputer.model.PatientDTO;
import com.example.bitcomputer.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @Mock
    PatientService patientService;

    @InjectMocks
    PatientController patientController;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
    }

    @Test
    @DisplayName("성공 시 201 Created와 patientId 반환")
    void create_success() throws Exception {
        PatientDTO saved = new PatientDTO();
        saved.setId(123);
        when(patientService.createPatient(any(PatientDTO.class))).thenReturn(saved);

        PatientDTO req = new PatientDTO();
        req.setName("홍길동");
        req.setPhoneNumber("010");
        req.setIdentityNumber("900101-1234567");
        req.setGender("M");
        req.setBirth(new java.util.Date());

        mockMvc.perform(post("/api/patients/get_patient_id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(123));
    }
}
