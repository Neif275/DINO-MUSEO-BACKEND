package com.dinomuseo.backend.model;

// Roles del sistema segun las instrucciones de la evaluacion
public enum Rol {
    ADMIN,     // acceso total
    VENDEDOR,  // ve entradas y boletas (lista y detalle)
    CLIENTE    // solo accede a la tienda
}
