package com.dinomuseo.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Se ejecuta en cada peticion: si trae "Authorization: Bearer <token>" y el token es valido,
// deja al usuario autenticado con su rol para que Spring Security revise los permisos.
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtFilter(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(7);

            // solo se acepta el token de acceso; el refresh token no sirve para entrar a los endpoints
            if (jwtService.esValido(token, JwtService.TIPO_ACCESO)) {
                try {
                    UserDetails usuario = usuarioDetailsService.loadUserByUsername(jwtService.extraerCorreo(token));
                    if (usuario.isEnabled()) {
                        UsernamePasswordAuthenticationToken autenticacion =
                                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                        autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    }
                } catch (UsernameNotFoundException e) {
                    // el usuario del token ya no existe: la peticion sigue sin autenticar
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
