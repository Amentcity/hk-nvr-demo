package com.example.hknvr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HkNvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(HkNvrApplication.class, args);
    }
}
