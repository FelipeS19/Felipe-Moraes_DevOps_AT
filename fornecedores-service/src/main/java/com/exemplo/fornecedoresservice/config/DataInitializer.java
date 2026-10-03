package com.exemplo.fornecedoresservice.config;

import com.exemplo.fornecedoresservice.model.Fornecedor;
import com.exemplo.fornecedoresservice.repository.FornecedorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(FornecedorRepository repository) {
        return args -> {
            repository.save(new Fornecedor("Fornecedor 1", "00.000.000/0001-01"));
            repository.save(new Fornecedor("Fornecedor 2", "00.000.000/0001-02"));
            repository.save(new Fornecedor("Fornecedor 3", "00.000.000/0001-03"));
            repository.save(new Fornecedor("Fornecedor 4", "00.000.000/0001-04"));
            repository.save(new Fornecedor("Fornecedor 5", "00.000.000/0001-05"));
        };
    }
}
