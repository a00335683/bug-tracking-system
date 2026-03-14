package com.tus.controllers;

import com.tus.db.models.User;
import com.tus.db.repos.UserRepository;
import com.tus.dtos.LoginRequestDto;
import com.tus.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto requestDto) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsername(),
                            requestDto.getPassword()
                    )
            );

            User user = userRepository.findByUsername(requestDto.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            String token = jwtUtil.generateToken(requestDto.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (DisabledException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Account is inactive");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }
}