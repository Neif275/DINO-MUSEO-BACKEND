package com.dinomuseo.backend.controller;

import com.dinomuseo.backend.dto.AuthResponse;
import com.dinomuseo.backend.dto.LoginRequest;
import com.dinomuseo.backend.dto.RefreshRequest;
import com.dinomuseo.backend.dto.RegistroRequest;
import com.dinomuseo.backend.model.Usuario;
import com.dinomuseo.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Login, registro y renovación del token JWT")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Inicia sesión y entrega el token JWT y el refresh token")
    @ApiResponse(responseCode = "200", description = "Login correcto")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Crea una cuenta de cliente")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @Operation(summary = "Entrega un token nuevo usando el refresh token")
    @ApiResponse(responseCode = "200", description = "Token renovado")
    @ApiResponse(responseCode = "401", description = "Refresh token inválido o vencido")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.renovar(request.refreshToken()));
    }

    @Operation(summary = "Datos del usuario que inició sesión")
    @ApiResponse(responseCode = "200", description = "Usuario actual")
    @ApiResponse(responseCode = "401", description = "Sin token o token vencido")
    @GetMapping("/perfil")
    public ResponseEntity<Usuario> perfil(Authentication authentication) {
        return ResponseEntity.ok(authService.perfil(authentication.getName()));
    }
}
