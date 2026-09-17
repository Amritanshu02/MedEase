package com.example.medlocal.security;

import com.example.medlocal.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize, @PostAuthorize etc.
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // We don't need CSRF for JWT based authentication
                .csrf().disable()
                // don't create session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                .authorizeHttpRequests(authz -> authz
                        // Allow all requests to access auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                );

        // Add a filter to validate the tokens with every request
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}