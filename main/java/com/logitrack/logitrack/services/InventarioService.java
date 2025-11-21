package com.logitrack.logitrack.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.logitrack.logitrack.dto.InventarioDTO;
import com.logitrack.logitrack.exceptions.BusinessException;
import com.logitrack.logitrack.entities.Inventario;
import com.logitrack.logitrack.repositories.InventarioRepository;
import com.logitrack.logitrack.repositories.ProductoRepository;
import com.logitrack.logitrack.repositories.BodegaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;

    @Transactional
    public List<InventarioDTO> findAll() {
        return inventarioRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventarioDTO findById(Long id) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Inventario no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        return convertToDto(inventario);
    }

    @Transactional
    public List<InventarioDTO> findByProductoId(Long productoId) {
        // Validar que el producto existe
        if (!productoRepository.existsById(productoId)) {
            throw new BusinessException("Producto no encontrado con id: " + productoId, HttpStatus.NOT_FOUND);
        }
        return inventarioRepository.findByProductoId(productoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<InventarioDTO> findByBodegaId(Long bodegaId) {
        // Validar que la bodega existe
        if (!bodegaRepository.existsById(bodegaId)) {
            throw new BusinessException("Bodega no encontrada con id: " + bodegaId, HttpStatus.NOT_FOUND);
        }
        return inventarioRepository.findByBodegaId(bodegaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventarioDTO findByProductoAndBodega(Long productoId, Long bodegaId) {
        Inventario inventario = inventarioRepository.findByProductoIdAndBodegaId(productoId, bodegaId).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Inventario no encontrado para producto: " + productoId + " y bodega: " + bodegaId, HttpStatus.NOT_FOUND));
        return convertToDto(inventario);
    }

    @Transactional
    public InventarioDTO save(InventarioDTO dto) {
        // Validaciones de negocio
        if (dto.getProductoId() == null) {
            throw new BusinessException("El id del producto es obligatorio", HttpStatus.BAD_REQUEST);
        }
        if (dto.getBodegaId() == null) {
            throw new BusinessException("El id de la bodega es obligatorio", HttpStatus.BAD_REQUEST);
        }
        if (dto.getStockActual() == null || dto.getStockActual() < 0) {
            throw new BusinessException("El stock actual no puede ser negativo", HttpStatus.BAD_REQUEST);
        }

        // Verificar que el producto existe
        if (!productoRepository.existsById(dto.getProductoId())) {
            throw new BusinessException("Producto no encontrado con id: " + dto.getProductoId(), HttpStatus.NOT_FOUND);
        }

        // Verificar que la bodega existe
        if (!bodegaRepository.existsById(dto.getBodegaId())) {
            throw new BusinessException("Bodega no encontrada con id: " + dto.getBodegaId(), HttpStatus.NOT_FOUND);
        }

        // Verificar que no existe inventario duplicado (mismo producto y bodega)
        if (inventarioRepository.existsByProductoIdAndBodegaId(dto.getProductoId(), dto.getBodegaId())) {
            throw new BusinessException("Ya existe inventario para el producto: " + dto.getProductoId() + " en la bodega: " + dto.getBodegaId(), HttpStatus.CONFLICT);
        }

        Inventario inventario = new Inventario();
        inventario.setProductoId(dto.getProductoId());
        inventario.setBodegaId(dto.getBodegaId());
        inventario.setStockActual(dto.getStockActual());

        Inventario guardado = inventarioRepository.save(inventario);
        return convertToDto(guardado);
    }

    @Transactional
    public InventarioDTO update(Long id, InventarioDTO dto) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Inventario no encontrado con id: " + id, HttpStatus.NOT_FOUND));

        // Validaciones de negocio
        if (dto.getStockActual() == null || dto.getStockActual() < 0) {
            throw new BusinessException("El stock actual no puede ser negativo", HttpStatus.BAD_REQUEST);
        }

        // Si cambia producto o bodega, verificar que existen
        if (dto.getProductoId() != null && !productoRepository.existsById(dto.getProductoId())) {
            throw new BusinessException("Producto no encontrado con id: " + dto.getProductoId(), HttpStatus.NOT_FOUND);
        }
        if (dto.getBodegaId() != null && !bodegaRepository.existsById(dto.getBodegaId())) {
            throw new BusinessException("Bodega no encontrada con id: " + dto.getBodegaId(), HttpStatus.NOT_FOUND);
        }

        // Verificar duplicado si cambia producto o bodega (excluyendo el mismo id)
        Long productoId = dto.getProductoId() != null ? dto.getProductoId() : inventario.getProductoId();
        Long bodegaId = dto.getBodegaId() != null ? dto.getBodegaId() : inventario.getBodegaId();

        inventarioRepository.findByProductoIdAndBodegaIdAndIdNot(productoId, bodegaId, id).stream()
                .findAny()
                .ifPresent(inv -> {
                    throw new BusinessException("Ya existe inventario para el producto: " + productoId + " en la bodega: " + bodegaId, HttpStatus.CONFLICT);
                });

        if (dto.getProductoId() != null) {
            inventario.setProductoId(dto.getProductoId());
        }
        if (dto.getBodegaId() != null) {
            inventario.setBodegaId(dto.getBodegaId());
        }
        if (dto.getStockActual() != null) {
            inventario.setStockActual(dto.getStockActual());
        }

        Inventario actualizado = inventarioRepository.save(inventario);
        return convertToDto(actualizado);
    }

    @Transactional
    public void delete(Long id) {
        if (!inventarioRepository.existsById(id)) {
            throw new BusinessException("Inventario no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        inventarioRepository.deleteById(id);
    }

    private InventarioDTO convertToDto(Inventario inventario) {
        InventarioDTO dto = new InventarioDTO();
        dto.setId(inventario.getId());
        dto.setProductoId(inventario.getProductoId());
        dto.setBodegaId(inventario.getBodegaId());
        dto.setStockActual(inventario.getStockActual());
        return dto;
    }
}
