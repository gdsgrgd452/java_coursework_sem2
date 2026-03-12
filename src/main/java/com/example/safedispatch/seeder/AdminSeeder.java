package com.example.safedispatch.seeder;

import com.example.safedispatch.model.Admin;
import com.example.safedispatch.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder { //Class to add test Admin users into the database when the application builds
    @Bean
    CommandLineRunner seedTeacher(AdminRepository adminRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            Admin admin = new Admin();
            admin.setUsername("e");
            admin.setPassword(passwordEncoder.encode("e"));
            admin.setRole("HR");
            adminRepo.save(admin);
        };
    }
}
