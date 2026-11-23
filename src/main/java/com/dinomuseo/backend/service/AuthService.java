package com.dinomuseo.backend.service;

import com.dinomuseo.backend.dto.AuthResponse;
import com.dinomuseo.backend.dto.LoginRequest;
import com.dinomuseo.backend.dto.RegistroRequest;
import com.dinomuseo.backend.exception.ReglaNegocioException;
import com.dinomuseo.backend.model.Rol;
import com.dinomuseo.backend.model.Usuario;
import com.dinomuseo.backend.repository.UsuarioRepository;
import com.dinomuseo.backend.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Revisa correo y contraseña (comparando con el hash BCrypt) y entrega los tokens
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        }
        if (!usuario.getActivo()) {
            throw new BadCredentialsException("La cuenta esta desactivada");
        }
        return respuesta(usuario);
    }

    // Cualquier persona puede crear una cuenta, pero siempre queda como CLIENTE
    public AuthResponse registrar(RegistroRequest request) {
        String correo = request.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ReglaNegocioException("Ya existe una cuenta con el correo " + correo);
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
        return respuesta(usuarioRepository.save(usuario));
    }

    // Con un refresh token valido se entregan tokens nuevos, sin pedir la contraseña otra vez
    public AuthResponse renovar(String refreshToken) {
        if (!jwtService.esValido(refreshToken, JwtService.TIPO_REFRESH)) {
            throw new BadCredentialsException("La sesion expiro, vuelve a iniciar sesion");
        }
        Usuario usuario = usuarioRepository.findByCorreo(jwtService.extraerCorreo(refreshToken))
                .filter(Usuario::getActivo)
                .orElseThrow(() -> new BadCredentialsException("La sesion expiro, vuelve a iniciar sesion"));
        return respuesta(usuario);
    }

    public Usuario perfil(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
    }

    private AuthResponse respuesta(Usuario usuario) {
        return new AuthResponse(
                jwtService.generarToken(usuario),
                jwtService.generarRefreshToken(usuario),
                jwtService.getExpiracionMs(),
                usuario);
    }
}
