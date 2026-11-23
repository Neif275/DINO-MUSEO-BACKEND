package com.dinomuseo.backend.service;

import com.dinomuseo.backend.exception.RecursoNoEncontradoException;
import com.dinomuseo.backend.exception.ReglaNegocioException;
import com.dinomuseo.backend.model.Producto;
import com.dinomuseo.backend.repository.BoletaRepository;
import com.dinomuseo.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final BoletaRepository boletaRepository;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoRepository productoRepository, BoletaRepository boletaRepository,
                           CategoriaService categoriaService) {
        this.productoRepository = productoRepository;
        this.boletaRepository = boletaRepository;
        this.categoriaService = categoriaService;
    }

    public List<Producto> listar(boolean soloActivos) {
        return soloActivos ? productoRepository.findByActivoTrue() : productoRepository.findAll();
    }

    public Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la entrada " + id));
    }

    public Producto crear(Producto producto) {
        producto.setId(null);
        producto.setCategoria(categoriaService.buscar(producto.getCategoria().getId()));
        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto producto = buscar(id);
        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setCupos(datos.getCupos());
        producto.setActivo(datos.getActivo() == null ? producto.getActivo() : datos.getActivo());
        producto.setCategoria(categoriaService.buscar(datos.getCategoria().getId()));
        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        Producto producto = buscar(id);
        if (boletaRepository.existsByDetallesProductoId(id)) {
            throw new ReglaNegocioException("Esta entrada ya tiene ventas. Desactivala en vez de eliminarla");
        }
        productoRepository.delete(producto);
    }
}