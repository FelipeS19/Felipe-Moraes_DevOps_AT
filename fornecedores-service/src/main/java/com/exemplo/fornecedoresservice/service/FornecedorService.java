package com.exemplo.fornecedoresservice.service;

import com.exemplo.fornecedoresservice.client.ProdutoClient;
import com.exemplo.fornecedoresservice.model.Fornecedor;
import com.exemplo.fornecedoresservice.model.ProdutoDTO;
import com.exemplo.fornecedoresservice.repository.FornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository repository;

    @Autowired
    private ProdutoClient produtoClient;

    public List<Fornecedor> listarTodos() {
        return repository.findAll();
    }

    public Optional<Fornecedor> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Fornecedor salvar(Fornecedor fornecedor) {
        return repository.save(fornecedor);
    }

    public List<ProdutoDTO> buscarProdutos() {
        return produtoClient.buscarTodos();
    }
}
