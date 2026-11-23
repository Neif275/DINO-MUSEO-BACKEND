package com.dinomuseo.backend.service;

import com.dinomuseo.backend.dto.BoletaRequest;
import com.dinomuseo.backend.exception.ReglaNegocioException;
import com.dinomuseo.backend.model.*;
import com.dinomuseo.backend.repository.BoletaRepository;
import com.dinomuseo.backend.repository.ProductoRepository;
import com.dinomuseo.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BoletaService boletaService;

    private Producto general;
    private Producto ninos;

    @BeforeEach
    void preparar(){
        Categoria individual = new Categoria(1L, "Entrada individual", null);
        general = new Producto(1L, "General", null, 10000, 5, true, individual);
        ninos = new Producto(2L, "Niños", null, 5000, 10, true, individual);
        Usuario cliente = new Usuario(3L, "Cliente", "cliente@dinomuseo.cl", "x", Rol.CLIENTE, true);
        when(usuarioRepository.findByCorreo("cliente@dinomuseo.cl")).thenReturn(Optional.of(cliente));
    }

    private BoletaRequest compra(BoletaRequest.Item... items){
        return new BoletaRequest(LocalDate.now().plusDays(1), List.of(items));
    }

    @Test
    void calculaElTotalYDescuentaLosCupos(){
        when(productoRepository.findById(1L)).thenReturn(Optional.of(general));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(ninos));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(i -> i.getArgument(0));

        Boleta boleta = boletaService.crear("cliente@dinomuseo.cl",
                compra(new BoletaRequest.Item(1L, 2), new BoletaRequest.Item(2L, 1)));

        assertEquals(25000, boleta.getTotal());
        assertEquals(2, boleta.getDetalles().size());
        assertEquals(3, general.getCupos());
        assertEquals(9, ninos.getCupos());
    }

    @Test
    void noPermiteComprarMasEntradasQueLosCupos() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(general));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> boletaService.crear("cliente@dinomuseo.cl", compra(new BoletaRequest.Item(1L, 6))));

        assertEquals("Solo quedan 5 cupos para General", error.getMessage());
        verify(boletaRepository, never()).save(any());
    }

    @Test
    void noPermiteComprarUnaEntradaDesactivada() {
        general.setActivo(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(general));

        assertThrows(ReglaNegocioException.class,
                () -> boletaService.crear("cliente@dinomuseo.cl", compra(new BoletaRequest.Item(1L, 1))));
    }


}
