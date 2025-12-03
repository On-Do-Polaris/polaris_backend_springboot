package com.skax.physicalrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PhysicalRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhysicalRiskApplication.class, args);
    }
}
