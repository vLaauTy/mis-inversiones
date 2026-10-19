package com.vlaauty.misinversiones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MisInversionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MisInversionesApplication.class, args);
    }
}
