package com.example.bitcomputer.service;

import com.example.bitcomputer.model.DiagnoseDTO;

import java.io.File;
import java.util.List;

public interface DiagnoseService {
    DiagnoseDTO getById(int id);
    List<DiagnoseDTO> search(String query, String code, String name);
    int uploadFromExcel(File file);
}

