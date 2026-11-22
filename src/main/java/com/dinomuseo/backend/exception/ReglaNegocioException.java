package com.dinomuseo.backend.exception;

// Se lanza cuando una operacion no cumple una regla del negocio,
// por ejemplo comprar mas entradas que los cupos disponibles (responde 409)
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
