package com.example.medlocal.controller;

import com.example.medlocal.model.User;
import com.example.medlocal.model.User.Role;
import com.example.medlocal.security.dtos.LoginRequest;
import com.example.medlocal.security.dtos.LoginResponse;
import com.example.medlocal.security.dtos.RegisterRequest;
import com.example.medlocal.service.UserService;
import com.example.medlocal.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User.Role role = User.Role.valueOf(registerRequest.getRole());
            User user = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .password(registerRequest.getPassword())
                    .role(role)
                    .build();
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully: " + registeredUser.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Incorrect username or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Assuming userDetails is a User (from our UserDetailsServiceImpl)
        String username = userDetails.getUsername();
        String role = ((org.springframework.security.core.userdetails.User) userDetails).getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .username(username)
                .role(role)
                .build();

        return ResponseEntity.ok(loginResponse);
    }

    // Example of a protected endpoint
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World! This is a protected endpoint.");
    }
}