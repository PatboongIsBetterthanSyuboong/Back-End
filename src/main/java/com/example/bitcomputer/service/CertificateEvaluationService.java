package com.example.bitcomputer.service;

import com.example.bitcomputer.model.CertificateEvaluationResultDTO;

public interface CertificateEvaluationService {

    /**
     * 진단서 증상-소견 추론 일치도 평가
     *
     * @param historyId          내원 ID (symptomDetail, memo를 로드)
     * @param medicalCertificate AI가 생성한 진단서 전문
     * @return                   평가 결과
     */
    CertificateEvaluationResultDTO evaluate(Integer historyId, String medicalCertificate);
}
