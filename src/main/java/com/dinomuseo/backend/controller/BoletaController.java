package com.dinomuseo.backend.controller;

import com.dinomuseo.backend.dto.BoletaRequest;
import com.dinomuseo.backend.model.Boleta;
import com.dinomuseo.backend.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Boletas", description = "Compras de entradas")
@RestController
@RequestMapping("/api/v1/boletas")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @Operation(summary = "Lista todas las boletas (VENDEDOR y ADMIN)")
    @GetMapping
    public ResponseEntity<List<Boleta>> listar() {
        return ResponseEntity.ok(boletaService.listar());
    }

    @Operation(summary = "Boletas del usuario que inició sesión (CLIENTE)")
    @GetMapping("/mis-boletas")
    public ResponseEntity<List<Boleta>> misBoletas(Authentication authentication) {
        return ResponseEntity.ok(boletaService.listarDelUsuario(authentication.getName()));
    }

    @Operation(summary = "Detalle de una boleta (VENDEDOR y ADMIN)")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{id}")
    public ResponseEntity<Boleta> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(boletaService.buscar(id));
    }

    @Operation(summary = "Compra entradas (CLIENTE)")
    @ApiResponse(responseCode = "201", description = "Boleta creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "No hay cupos suficientes o la entrada no está disponible")
    @PostMapping
    public ResponseEntity<Boleta> crear(@Valid @RequestBody BoletaRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boletaService.crear(authentication.getName(), request));
    }

    @Operation(summary = "Anula una boleta y devuelve los cupos (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Anulada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boletaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}