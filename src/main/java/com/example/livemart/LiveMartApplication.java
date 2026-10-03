package com.example.livemart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LiveMartApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveMartApplication.class, args);
    }

}
