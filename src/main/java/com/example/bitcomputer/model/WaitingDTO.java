package com.example.bitcomputer.model;

import lombok.Data;

import java.util.Date;

@Data
public class WaitingDTO {
    int id;
    int patientId;
    int deptId;
    String symptom;
    Date entryDate;
    String state;
}
