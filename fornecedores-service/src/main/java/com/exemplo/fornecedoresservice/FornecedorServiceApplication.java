package com.exemplo.fornecedoresservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FornecedorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FornecedorServiceApplication.class, args);
    }

}
