package com.example.bitcomputer.Repository;

import com.example.bitcomputer.entity.RadiologyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RadiologyReportRepository extends JpaRepository<RadiologyReport, Integer> {
}

