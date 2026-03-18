package com.jbank.exceptions;

public class ValorInvalidoException extends Exception {
    public ValorInvalidoException( double valor ) {
        super("Não é possível a entrada de Valores NEGATIVOS: R$" + valor);
    }

}
