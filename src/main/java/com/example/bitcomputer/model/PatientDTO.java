package com.example.bitcomputer.model;

import lombok.Data;

import java.util.Date;

@Data
public class PatientDTO {
    int id;
    String name;
    String phoneNumber;
    String identityNumber;
    Date birth;
    String gender;
}
