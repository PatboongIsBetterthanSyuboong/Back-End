package com.example.bitcomputer.controller;

import com.example.bitcomputer.Repository.DiseaseRepository;
import com.example.bitcomputer.entity.Disease;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DiseaseControllerTest {

    MockMvc mockMvc;

    @Mock
    DiseaseRepository diseaseRepository;

    @InjectMocks
    DiseaseController diseaseController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(diseaseController).build();
    }

    @Nested
    @DisplayName("GET /api/diseases/{code}")
    class GetByCode {
        @Test
        @DisplayName("존재 시 200 OK + DTO 반환")
        void success() throws Exception {
            // 임의 상병 코드
            Disease entity = new Disease(1, "J00", "급성 비인두염");
            when(diseaseRepository.findByCode(eq("J00"))).thenReturn(Optional.of(entity));

            mockMvc.perform(get("/api/diseases/J00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.code").value("J00"))
                    .andExpect(jsonPath("$.name").value("급성 비인두염"));
        }

        @Test
        @DisplayName("404 Not Found")
        void notFound() throws Exception {
            when(diseaseRepository.findByCode(eq("XXX"))).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/diseases/XXX"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/diseases?query=... / code=... / name=...")
    class Search {
        @Test
        @DisplayName("query로 code/name 부분검색")
        void search_by_query() throws Exception {
            when(diseaseRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(eq("J0"), eq("J0")))
                    .thenReturn(List.of(new Disease(1, "J00", "급성 비인두염")));

            mockMvc.perform(get("/api/diseases").param("query", "J0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].code").value("J00"));
        }

        @Test
        @DisplayName("code/name 조합으로 부분검색")
        void search_by_code_name() throws Exception {
            when(diseaseRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(eq("J0"), eq("비인두")))
                    .thenReturn(List.of(new Disease(1, "J00", "급성 비인두염")));

            mockMvc.perform(get("/api/diseases").param("code", "J0").param("name", "비인두"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("급성 비인두염"));
        }
    }
}