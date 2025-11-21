package com.logitrack.logitrack.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.logitrack.logitrack.dto.BodegaDTO;
import com.logitrack.logitrack.exceptions.BusinessException;
import com.logitrack.logitrack.entities.Bodega;
import com.logitrack.logitrack.entities.Usuario;
import com.logitrack.logitrack.repositories.BodegaRepository;
import com.logitrack.logitrack.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BodegaService {

    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public List<BodegaDTO> findAll() {
        return bodegaRepository.findAllActive().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BodegaDTO findById(Long id) {
        Bodega bodega = bodegaRepository.findByIdActive(id)
                .orElseThrow(() -> new BusinessException("Bodega no encontrada con id: " + id, HttpStatus.NOT_FOUND));
        return convertToDto(bodega);
    }

    @Transactional
    public List<BodegaDTO> findByEncargadoId(Long id) {
        return bodegaRepository.findByEncargadoId(id).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BodegaDTO> findByEncargadoNombre(String nombre) {
        return bodegaRepository.findByEncargadoNombre(nombre).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BodegaDTO> findByCapacidadGreaterThan(Integer capacidad) {
        return bodegaRepository.findByCapacidadGreaterThan(capacidad).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BodegaDTO save(BodegaDTO dto) {
        //Buscar el encargado de la bodega
        Usuario usuario = usuarioRepository.findAdminById(dto.getEncargadoId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado o Usuario sin rol de ADMIN con id: " + dto.getEncargadoId()));
        
        Bodega bodega = new Bodega();
        bodega.setNombre(dto.getNombre());
        bodega.setUbicacion(dto.getUbicacion());
        bodega.setCapacidad(dto.getCapacidad());
        bodega.setEncargado(usuario);
        bodega.setActivo(true);

        Bodega saved = bodegaRepository.save(bodega);
        return convertToDto(saved);
    }

    @Transactional
    public BodegaDTO update(Long id, BodegaDTO dto) {
        Bodega bodega = bodegaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada con id: " + id));
            
        if (!bodega.getEncargado().getId().equals(dto.getEncargadoId())) {
            Usuario nuevoUsuario = usuarioRepository.findById(dto.getEncargadoId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + dto.getEncargadoId()));
            bodega.setEncargado(nuevoUsuario);
        }

        bodega.setNombre(dto.getNombre());
        bodega.setCapacidad(dto.getCapacidad());
        bodega.setActivo(dto.isActivo());
        bodega.setUbicacion(dto.getUbicacion());

        Bodega uptaded = bodegaRepository.save(bodega);
        return convertToDto(uptaded);
    }

    @Transactional
    public void delete(Long id) {
        if (!bodegaRepository.existsById(id)) {
            throw new BusinessException("Bodega no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        bodegaRepository.softDeleteById(id);
    }

    private BodegaDTO convertToDto(Bodega bodega) {
        BodegaDTO dto = new BodegaDTO();
        dto.setId(bodega.getId());
        dto.setNombre(bodega.getNombre());
        dto.setUbicacion(bodega.getUbicacion());
        dto.setCapacidad(bodega.getCapacidad());
        dto.setEncargadoId(bodega.getEncargado().getId());
        dto.setActivo(bodega.isActivo());
        dto.setCreatedAt(bodega.getCreatedAt());
        dto.setUpdaateAt(bodega.getUpdateAt());
        return dto;
    }
}
