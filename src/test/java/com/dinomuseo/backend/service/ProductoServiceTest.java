package com.dinomuseo.backend.service;

import com.dinomuseo.backend.exception.RecursoNoEncontradoException;
import com.dinomuseo.backend.exception.ReglaNegocioException;
import com.dinomuseo.backend.model.Categoria;
import com.dinomuseo.backend.model.Producto;
import com.dinomuseo.backend.repository.BoletaRepository;
import com.dinomuseo.backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private BoletaRepository boletaRepository;
    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private ProductoService productoService;

    private final Producto general = new Producto(1L, "General", null, 10000, 200, true,
            new Categoria(1L, "Entrada individual", null));

    @Test
    void noEliminaUnaEntradaQueYaTieneVentas() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(general));
        when(boletaRepository.existsByDetallesProductoId(1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> productoService.eliminar(1L));
        verify(productoRepository, never()).delete(any());
    }

    @Test
    void eliminaUnaEntradaSinVentas() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(general));
        when(boletaRepository.existsByDetallesProductoId(1L)).thenReturn(false);

        productoService.eliminar(1L);

        verify(productoRepository).delete(general);
    }

    @Test
    void buscarUnaEntradaQueNoExisteLanzaError() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.buscar(99L));
    }
}