package com.dinomuseo.backend.config;

import com.dinomuseo.backend.model.Rol;
import com.dinomuseo.backend.model.Usuario;
import com.dinomuseo.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Al iniciar el backend crea un usuario de cada rol si la tabla esta vacia,
// para poder probar el login sin crear cuentas a mano.
@Component
public class UsuariosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuariosIniciales(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        crear("Administrador", "admin@dinomuseo.cl", "Admin123!", Rol.ADMIN);
        crear("Vendedor", "vendedor@dinomuseo.cl", "Vendedor123!", Rol.VENDEDOR);
        crear("Cliente de prueba", "cliente@dinomuseo.cl", "Cliente123!", Rol.CLIENTE);
    }

    private void crear(String nombre, String correo, String password, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
}
