package com.dinomuseo.backend.service;

import com.dinomuseo.backend.dto.BoletaRequest;
import com.dinomuseo.backend.exception.RecursoNoEncontradoException;
import com.dinomuseo.backend.exception.ReglaNegocioException;
import com.dinomuseo.backend.model.Boleta;
import com.dinomuseo.backend.model.DetalleBoleta;
import com.dinomuseo.backend.model.Producto;
import com.dinomuseo.backend.model.Usuario;
import com.dinomuseo.backend.repository.BoletaRepository;
import com.dinomuseo.backend.repository.ProductoRepository;
import com.dinomuseo.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public BoletaService(BoletaRepository boletaRepository, ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Boleta> listar() {
        return boletaRepository.findAllByOrderByFechaDesc();
    }

    public List<Boleta> listarDelUsuario(String correo) {
        return boletaRepository.findByUsuarioCorreoOrderByFechaDesc(correo);
    }

    public Boleta buscar(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la boleta " + id));
    }

    @Transactional
    public Boleta crear(String correoUsuario, BoletaRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + correoUsuario));

        Boleta boleta = new Boleta();
        boleta.setUsuario(usuario);
        boleta.setFecha(LocalDateTime.now());
        boleta.setFechaVisita(request.fechaVisita());

        int total = 0;
        for (BoletaRequest.Item item : request.items()) {
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe la entrada " + item.productoId()));

            if (!producto.getActivo()) {
                throw new ReglaNegocioException("La entrada " + producto.getNombre() + " no esta disponible");
            }
            if (producto.getCupos() < item.cantidad()) {
                throw new ReglaNegocioException("Solo quedan " + producto.getCupos()
                        + " cupos para " + producto.getNombre());
            }

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setBoleta(boleta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio() * item.cantidad());
            boleta.getDetalles().add(detalle);

            producto.setCupos(producto.getCupos() - item.cantidad());
            productoRepository.save(producto);
            total += detalle.getSubtotal();
        }

        boleta.setTotal(total);
        return boletaRepository.save(boleta);
    }

    @Transactional
    public void eliminar(Long id) {
        Boleta boleta = buscar(id);
        for (DetalleBoleta detalle : boleta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setCupos(producto.getCupos() + detalle.getCantidad());
            productoRepository.save(producto);
        }
        boletaRepository.delete(boleta);
    }
}



