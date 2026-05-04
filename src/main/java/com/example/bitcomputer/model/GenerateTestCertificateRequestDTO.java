package com.example.bitcomputer.model;

import lombok.Data;

@Data
public class GenerateTestCertificateRequestDTO {
    private String diseaseCode;
    private String prescriptionCode;
    private String prescriptionName;
}
