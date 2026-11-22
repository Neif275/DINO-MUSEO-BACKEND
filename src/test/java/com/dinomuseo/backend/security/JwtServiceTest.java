package com.dinomuseo.backend.security;

import com.dinomuseo.backend.model.Rol;
import com.dinomuseo.backend.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRETO = "zjYq4qK//wvxH2Ef2xkvQkMIVWbP26prJwd9o9ueTvNweTfm0CUxhSBREYTezyMgRRAfPQdFBTW4PD7TCwphqQ==";

    private final JwtService jwtService = new JwtService(SECRETO, 60_000, 120_000);

    private Usuario usuario() {
        return new Usuario(1L, "Vendedor", "vendedor@dinomuseo.cl", "x", Rol.VENDEDOR, true);
    }

    @Test
    void generaUnTokenConElCorreoYElRol() {
        String token = jwtService.generarToken(usuario());

        assertEquals("vendedor@dinomuseo.cl", jwtService.extraerCorreo(token));
        assertEquals("VENDEDOR", jwtService.leer(token).get("rol", String.class));
        assertTrue(jwtService.esValido(token, JwtService.TIPO_ACCESO));
    }

    @Test
    void elRefreshTokenNoSirveComoTokenDeAcceso() {
        String refresh = jwtService.generarRefreshToken(usuario());

        assertTrue(jwtService.esValido(refresh, JwtService.TIPO_REFRESH));
        assertFalse(jwtService.esValido(refresh, JwtService.TIPO_ACCESO));
    }

    @Test
    void rechazaUnTokenModificado() {
        String token = jwtService.generarToken(usuario());
        String modificado = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtService.esValido(modificado, JwtService.TIPO_ACCESO));
    }

    @Test
    void rechazaUnTokenVencido() {
        JwtService duraCero = new JwtService(SECRETO, -1000, -1000);
        String vencido = duraCero.generarToken(usuario());

        assertFalse(jwtService.esValido(vencido, JwtService.TIPO_ACCESO));
    }
}
