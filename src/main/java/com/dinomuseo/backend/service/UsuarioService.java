package com.dinomuseo.backend.service;

import com.dinomuseo.backend.exception.RecursoNoEncontradoException;
import com.dinomuseo.backend.exception.ReglaNegocioException;
import com.dinomuseo.backend.model.Usuario;
import com.dinomuseo.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + id));
    }

    public Usuario crear(Usuario usuario) {
        String correo = usuario.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ReglaNegocioException("Ya existe una cuenta con el correo " + correo);
        }
        if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 6 caracteres");
        }
        if (usuario.getRol() == null) {
            throw new ReglaNegocioException("El rol es obligatorio");
        }
        usuario.setId(null);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }
        return usuarioRepository.save(usuario);
    }

    // La contraseña solo se cambia si viene en la peticion
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario usuario = buscar(id);
        usuario.setNombre(datos.getNombre());
        usuario.setCorreo(datos.getCorreo().trim().toLowerCase());
        if (datos.getRol() != null) {
            usuario.setRol(datos.getRol());
        }
        if (datos.getActivo() != null) {
            usuario.setActivo(datos.getActivo());
        }
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.delete(buscar(id));
    }
}
