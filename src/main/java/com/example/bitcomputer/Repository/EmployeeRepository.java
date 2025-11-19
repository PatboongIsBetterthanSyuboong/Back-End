package com.example.bitcomputer.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bitcomputer.entity.Employee;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findAll();
    Employee findById(int id);
    Employee findByUsername(String username);
}
