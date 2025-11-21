package com.logitrack.logitrack.services;

import com.logitrack.logitrack.dto.LoginRequestDTO;
import com.logitrack.logitrack.dto.JwtResponseDTO;
import com.logitrack.logitrack.entities.Usuario;
import com.logitrack.logitrack.repositories.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoderService passwordEncoderService;

    public AuthService(AuthenticationManager authenticationManager,
                      CustomUserDetailsService userDetailsService,
                      JwtService jwtService,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoderService passwordEncoderService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoderService = passwordEncoderService;
    }

    public JwtResponseDTO login(LoginRequestDTO loginRequest) {
        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Obtener UserDetails y generar token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);
        
        // Obtener usuario completo para la respuesta
        Usuario usuario = userDetailsService.loadUsuarioByEmail(loginRequest.getEmail());
        
        return new JwtResponseDTO(jwt, usuario);
    }

    public boolean validarCredenciales(String email, String password) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            return passwordEncoderService.matches(password, usuario.getPasswordHash()) 
                   && usuario.getActivo();
        } catch (Exception e) {
            return false;
        }
    }

    public Usuario getUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }
        throw new RuntimeException("Usuario no autenticado");
    }

    public boolean tieneRol(String rol) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.getAuthorities().stream()
                       .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
}