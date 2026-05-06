package com.example.bitcomputer.service;

import com.example.bitcomputer.model.PrescriptionRecommendRequestDTO;
import com.example.bitcomputer.model.PrescriptionRecommendResponseDTO;
import com.example.bitcomputer.model.SavePrescriptionFeedbackRequestDTO;

public interface AgentService {

    PrescriptionRecommendResponseDTO recommendPrescription(PrescriptionRecommendRequestDTO request);

    void savePrescriptionFeedback(SavePrescriptionFeedbackRequestDTO request);
}
