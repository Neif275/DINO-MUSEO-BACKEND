package com.dinomuseo.backend.repository;

import com.dinomuseo.backend.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findByUsuarioCorreoOrderByFechaDesc(String correo);

    boolean existsByDetallesProductoId(Long productoId);
}