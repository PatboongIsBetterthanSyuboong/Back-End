package com.example.bitcomputer.Repository;

import com.example.bitcomputer.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    boolean existsById(String id);
    Patient findById(String id);
}

