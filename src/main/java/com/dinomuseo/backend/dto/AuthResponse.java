package com.dinomuseo.backend.dto;

import com.dinomuseo.backend.model.Usuario;

// Lo que recibe el frontend al iniciar sesion, registrarse o renovar el token
public record AuthResponse(
        String token,
        String refreshToken,
        long expiraEnMs,
        Usuario usuario
) {
}
