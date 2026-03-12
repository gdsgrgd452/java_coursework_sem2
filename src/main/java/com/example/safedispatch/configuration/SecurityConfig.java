package com.example.safedispatch.configuration;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService customUserDetailsService) {
        this.userDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        try {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                            // Employee Management (HR Only for modifications)
                            .requestMatchers(HttpMethod.POST, "/api/employees/**").hasRole("HR")
                            .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasRole("HR")
                            .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("HR")

                            // Department Management
                            .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("HR")

                            // Assignments & Business Logic
                            .requestMatchers(HttpMethod.POST, "/api/assignments/**").hasRole("HR")
                            .requestMatchers(HttpMethod.DELETE, "/api/assignments/**").hasRole("HR")
                            .requestMatchers(HttpMethod.PUT, "/api/employees/*/promote").hasRole("HR")

                            // Read Access (HR and Manager)
                            .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("HR", "MANAGER")
                            .requestMatchers(HttpMethod.GET, "/api/departments/**").hasAnyRole("HR", "MANAGER")

                            .anyRequest().authenticated()
                    )
                    .httpBasic(Customizer.withDefaults())
                    .exceptionHandling(e -> e
                            .accessDeniedPage("/denied")
                    )
                    .userDetailsService(userDetailsService);

            return http.build();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() { //Initialises a new password encoder
        return new BCryptPasswordEncoder();
    }
}