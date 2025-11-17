package com.example.bitcomputer.service;

import com.example.bitcomputer.model.DiseaseDTO;

import java.io.File;
import java.util.List;

public interface DiseaseService {
    DiseaseDTO getById(int id);
    List<DiseaseDTO> search(String query, String code, String name);
    int uploadFromExcel(File file);
}

