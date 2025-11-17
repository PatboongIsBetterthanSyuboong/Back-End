package com.example.bitcomputer.Repository;

import com.example.bitcomputer.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface DiseaseRepository extends JpaRepository<Disease, Integer> {
    Optional<Disease> findByCode(String code);
    Optional<Disease> findByName(String name);
    List<Disease> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name);
}