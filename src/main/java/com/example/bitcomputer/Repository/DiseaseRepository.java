package com.example.bitcomputer.Repository;

import com.example.bitcomputer.entity.Disease;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiseaseRepository extends JpaRepository<Disease, Integer> {
    Optional<Disease> findByCode(String code);
    Optional<Disease> findByName(String name);
    Page<Disease> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name, Pageable pageable);
}