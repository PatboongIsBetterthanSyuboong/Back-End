package com.example.bitcomputer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(
        basePackages = "com.example.bitcomputer.Repository",
        excludeFilters = @Filter(
                type = FilterType.REGEX,
                pattern = "com\\.example\\.bitcomputer\\.Repository\\.arango\\..*"
        )
)
public class BitComputerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BitComputerApplication.class, args);
    }

}
