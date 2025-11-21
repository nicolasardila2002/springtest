package com.logitrack.logitrack.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.logitrack.logitrack.dto.ProductoDTO;
import com.logitrack.logitrack.exceptions.BusinessException;
import com.logitrack.logitrack.entities.Producto;
import com.logitrack.logitrack.repositories.ProductoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional
    public List<ProductoDTO> findAll() {
        return productoRepository.findAllActive().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoDTO findById(Long id) {
        Producto producto = productoRepository.findByIdActive(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        return convertToDto(producto);
    }

    @Transactional
    public List<ProductoDTO> findByNombre(String nombre) {
        return productoRepository.findByNombre(nombre).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ProductoDTO> findByPrecioGreaterThan(BigDecimal precio) {
        return productoRepository.findByPrecioGreaterThan(precio).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ProductoDTO> findByCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoDTO save(ProductoDTO dto) {
        // Validaciones de negocio
        if (dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor que 0", HttpStatus.BAD_REQUEST);
        }

        if (productoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new BusinessException("Ya existe un producto con el nombre: " + dto.getNombre(), HttpStatus.CONFLICT);
        }

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setCategoria(dto.getCategoria());
        producto.setPrecio(dto.getPrecio());
        // Inicializar activo por defecto
        producto.setActivo(true);

        Producto guardado = productoRepository.save(producto);
        return convertToDto(guardado);
    }

    @Transactional
    public ProductoDTO update(Long id, ProductoDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        // Validaciones de negocio
        if (dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor que 0", HttpStatus.BAD_REQUEST);
        }

        // Verificar duplicado por nombre (excluyendo el mismo id)
        productoRepository.findByNombreIgnoreCase(dto.getNombre()).stream()
                .filter(p -> !p.getId().equals(id))
                .findAny()
                .ifPresent(p -> {
                    throw new BusinessException("Otro producto ya existe con el nombre: " + dto.getNombre(), HttpStatus.CONFLICT);
                });

        producto.setNombre(dto.getNombre());
        producto.setCategoria(dto.getCategoria());
        producto.setPrecio(dto.getPrecio());
        producto.setActivo(dto.isActivo());

        Producto actualizado = productoRepository.save(producto);
        return convertToDto(actualizado);
    }

    @Transactional
    public void delete(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new BusinessException("Producto no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        productoRepository.softDeleteById(id);
    }

    private ProductoDTO convertToDto(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setCategoria(producto.getCategoria());
        dto.setPrecio(producto.getPrecio());
        dto.setActivo(producto.isActivo());
        dto.setCreatedAt(producto.getCreatedAt());
        dto.setUpdateAt(producto.getUpdateAt());
        return dto;
    }
}
