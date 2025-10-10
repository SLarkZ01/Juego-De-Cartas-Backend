package com.juegocartas.juegocartas.service;

import com.juegocartas.juegocartas.dto.request.LoginRequest;
import com.juegocartas.juegocartas.dto.request.RegisterRequest;
import com.juegocartas.juegocartas.dto.response.AuthResponse;
import com.juegocartas.juegocartas.exception.BadRequestException;
import com.juegocartas.juegocartas.model.Usuario;
import com.juegocartas.juegocartas.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService,
                      AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // Validar que no exista el usuario o email
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("El nombre de usuario ya está en uso");
        }
        
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setActivo(true);

        usuarioRepository.save(usuario);

        // Generar token
        String token = jwtService.generateToken(usuario);

        return new AuthResponse(
            token,
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Determinar si es email o username
        String usernameOrEmail = request.getUsername();
        Usuario usuario = null;
        
        // Intentar buscar por username o email
        if (usernameOrEmail.contains("@")) {
            // Es un email
            usuario = usuarioRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));
        } else {
            // Es un username
            usuario = usuarioRepository.findByUsername(usernameOrEmail)
                    .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));
        }
        
        // Autenticar (usar el username real del usuario encontrado)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                usuario.getUsername(),
                request.getPassword()
            )
        );

        // Generar token
        String token = jwtService.generateToken(usuario);

        return new AuthResponse(
            token,
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail()
        );
    }
}
