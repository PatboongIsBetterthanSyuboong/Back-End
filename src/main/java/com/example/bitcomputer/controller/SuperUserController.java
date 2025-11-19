package com.example.bitcomputer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bitcomputer.Repository.EmployeeRepository;
import com.example.bitcomputer.entity.Employee;
import com.example.bitcomputer.entity.Role;
import com.example.bitcomputer.model.RoleUpdateDTO;

@RestController
@RequestMapping("/api/super")
public class SuperUserController {

    private final EmployeeRepository employeeRepository;

    public SuperUserController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @PutMapping("/set_role/{id}")
    public ResponseEntity<String> setRole(@PathVariable int id, @RequestBody RoleUpdateDTO request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        String username = authentication.getName();
        Employee requester = employeeRepository.findByUsername(username);
        if (requester == null || requester.getRole() != Role.SUPER_USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("SUPER_USER만 역할 변경이 가능합니다.");
        }

        Employee employee = employeeRepository.findById(id);
        if(employee == null) {
            return ResponseEntity.notFound().build();
        }

        if (request == null || request.getRole() == null || request.getRole().isBlank()) {
            return ResponseEntity.badRequest().body("role 값이 필요합니다.");
        }

        Role newRole;
        try {
            newRole = Role.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("유효하지 않은 role 값입니다.");
        }

        employee.setRole(newRole);
        employeeRepository.save(employee);
        return ResponseEntity.ok("Role set successfully");
    }

    @GetMapping("/get_all_users")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeRepository.findAll());
    }
}
