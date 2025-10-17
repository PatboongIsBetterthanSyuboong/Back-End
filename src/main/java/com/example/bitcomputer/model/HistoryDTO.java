package com.example.bitcomputer.model;

import lombok.Data;

import java.util.Date;

@Data
public class HistoryDTO {
    int id;
    int employeeId;
    int patientId;
    int deptId;
    String symptomDetail;
    String memo;
    Date entryDate;
}
