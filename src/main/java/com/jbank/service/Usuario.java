package com.jbank.service;

import com.jbank.model.Conta;

import java.util.ArrayList;
import java.util.List;



public class Usuario {

    private int id;
    private String nome;
    private String senha;
    private List<Conta> contas;


    public Usuario( int id, String nome, String senha ) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
        this.contas = new ArrayList<>();
    }

    public void setContas ( List<Conta> contas ) { this.contas = contas; }

    public int getId() { return this.id; }
    public String getNome() { return this.nome; }
    public String getSenha() { return this.senha; }
    public List<Conta> getContas() { return contas; }

}
