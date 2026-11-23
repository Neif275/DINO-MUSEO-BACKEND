package com.dinomuseo.backend.controller;

import com.dinomuseo.backend.model.Producto;
import com.dinomuseo.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Entradas", description = "Productos de la tienda (tipos de entrada). Leer es público, modificar solo ADMIN")
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Lista las entradas. Con soloActivos=true devuelve solo las que se venden")
    @GetMapping
    public ResponseEntity<List<Producto>> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return ResponseEntity.ok(productoService.listar(soloActivos));
    }

    @Operation(summary = "Detalle de una entrada")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscar(id));
    }

    @Operation(summary = "Crea una entrada (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(producto));
    }

    @Operation(summary = "Actualiza una entrada (ADMIN)")
    @ApiResponse(responseCode = "404", description = "No existe")
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    @Operation(summary = "Elimina una entrada sin ventas (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Eliminada")
    @ApiResponse(responseCode = "409", description = "Tiene ventas: hay que desactivarla")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}