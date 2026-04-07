package com.example.bitcomputer.service;

import com.example.bitcomputer.model.PrescriptionRecommendRequestDTO;
import com.example.bitcomputer.model.PrescriptionRecommendResponseDTO;

public interface AgentService {

    PrescriptionRecommendResponseDTO recommendPrescription(PrescriptionRecommendRequestDTO request);
}
