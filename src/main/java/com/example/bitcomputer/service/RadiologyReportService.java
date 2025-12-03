package com.example.bitcomputer.service;

import com.example.bitcomputer.model.RadiologyReportRequestDTO;
import com.example.bitcomputer.model.RadiologyReportResponseDTO;

public interface RadiologyReportService {
    RadiologyReportResponseDTO processRadiologyReport(RadiologyReportRequestDTO request);
}
