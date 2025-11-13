package com.skax.physicalrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PhysicalRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhysicalRiskApplication.class, args);
    }
}
