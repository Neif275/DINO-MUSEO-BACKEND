package com.dinomuseo.backend.security;

import com.dinomuseo.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

// Genera, lee y valida los tokens JWT.
// Hay 2 tipos: el token de acceso (dura 30 min) y el refresh token (dura 7 dias),
// que solo sirve para pedir un token de acceso nuevo.
@Service
public class JwtService {

    public static final String TIPO_ACCESO = "acceso";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final long expiracionMs;
    private final long refreshExpiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiracion-ms}") long expiracionMs,
                      @Value("${jwt.refresh-expiracion-ms}") long refreshExpiracionMs) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        this.expiracionMs = expiracionMs;
        this.refreshExpiracionMs = refreshExpiracionMs;
    }

    public String generarToken(Usuario usuario) {
        return construir(usuario, TIPO_ACCESO, expiracionMs);
    }

    public String generarRefreshToken(Usuario usuario) {
        return construir(usuario, TIPO_REFRESH, refreshExpiracionMs);
    }

    private String construir(Usuario usuario, String tipo, long duracionMs) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombre())
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave)
                .compact();
    }

    // Lee el contenido del token. Si la firma no coincide o el token vencio, lanza JwtException
    public Claims leer(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerCorreo(String token) {
        return leer(token).getSubject();
    }

    // true si la firma es valida, no esta vencido y es del tipo esperado
    public boolean esValido(String token, String tipoEsperado) {
        try {
            return tipoEsperado.equals(leer(token).get("tipo", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }
}
