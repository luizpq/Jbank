package com.jbank.model;

import com.jbank.exceptions.SaldoInsuficinteException;
import com.jbank.exceptions.ValorInvalidoException;

public abstract class Conta {
    private String titular;
    private int numero;
    private double saldo;


    public void sacar( double valor ) throws SaldoInsuficinteException, ValorInvalidoException {
        if ( valor <= this.saldo ) {
            this.saldo -= valor;

        } else if ( valor <= 0 ) {
            throw new ValorInvalidoException(valor);

        } else {
            throw new SaldoInsuficinteException( valor );
        }
    }

    public void depositar( double valor ) throws ValorInvalidoException {
        if ( valor <= 0 ) {
            throw new ValorInvalidoException( valor );
        }

        this.saldo += valor;
    }

    public void transferir( double valor, Conta destino ) throws SaldoInsuficinteException, ValorInvalidoException {
        this.sacar( valor );

        destino.depositar( valor );
    }

    public Conta( String titular, int numero, double saldo ) {
        this.titular = titular;
        this.numero = numero;
        this.saldo = saldo;
    }

    public String getTitular() { return this.titular; }
    public int getNumero() { return this.numero; }
    public double getSaldo() { return this.saldo; }

}
