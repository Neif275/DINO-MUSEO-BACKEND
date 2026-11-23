package com.dinomuseo.backend.service;

import com.dinomuseo.backend.exception.RecursoNoEncontradoException;
import com.dinomuseo.backend.exception.ReglaNegocioException;
import com.dinomuseo.backend.model.Categoria;
import com.dinomuseo.backend.repository.CategoriaRepository;
import com.dinomuseo.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoria " + id));
    }

    public Categoria crear(Categoria categoria) {
        if (categoriaRepository.existsByNombre(categoria.getNombre())){
            throw new ReglaNegocioException("Ya existe la categoria " + categoria.getNombre());
        }
        categoria.setId(null);
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria datos){
        Categoria categoria = buscar(id);
        categoria.setNombre(datos.getNombre());
        categoria.setDescripcion(datos.getDescripcion());
        return categoriaRepository.save(categoria);
    }

    public void eliminar (Long id) {
        Categoria categoria = buscar(id);
        if (productoRepository.existsByCategoriaId(id)){
            throw new ReglaNegocioException("La categoria tiene entradas asociadas, eliminalas o muevelas primero");
        }
        categoriaRepository.delete(categoria);
    }

}
