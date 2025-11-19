package com.example.bitcomputer.config;

import com.example.bitcomputer.Repository.UserRepository;
import com.example.bitcomputer.entity.Employee;
import com.example.bitcomputer.entity.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// 임시 더미 데이터 추가하는 코드이니깐 나중에 dept 구현 시에 없애야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeDummyDept(JdbcTemplate jdbcTemplate) {
        return args -> {
            // dept 테이블에 id=1 이 없으면 생성 (UNASSIGNED)
            String upsertSql = "INSERT INTO dept (id, dept) VALUES (1, 'UNASSIGNED') " +
                    "ON DUPLICATE KEY UPDATE dept = VALUES(dept)";
            jdbcTemplate.update(upsertSql);
        };
    }
}


