package com.dinomuseo.backend.config;

import com.dinomuseo.backend.model.Categoria;
import com.dinomuseo.backend.model.Producto;
import com.dinomuseo.backend.repository.CategoriaRepository;
import com.dinomuseo.backend.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogoInicial implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CatalogoInicial(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }

        Categoria individual = categoriaRepository.save(new Categoria(null, "Entrada individual",
                "Entrada para recorrer el museo por cuenta propia"));
        Categoria guiada = categoriaRepository.save(new Categoria(null, "Visita guiada",
                "Recorrido con guía para grupos y delegaciones de colegios"));

        crear("General", "A partir de los 15 años", 10000, 200, individual);
        crear("Niños", "Entre 3 y 14 años", 5000, 200, individual);
        crear("Adulto mayor", "A partir de los 65 años", 0, 100, individual);
        crear("Menor de 3 años", "Niños menores de 3 años", 0, 100, individual);
        crear("Visita guiada", "Recorrido de 90 minutos con guía, por persona", 12000, 40, guiada);
        crear("Delegación de colegio", "Visita guiada para colegios, por estudiante", 4000, 120, guiada);
    }

    private void crear(String nombre, String descripcion, int precio, int cupos, Categoria categoria) {
        productoRepository.save(new Producto(null, nombre, descripcion, precio, cupos, true, categoria));
    }
}