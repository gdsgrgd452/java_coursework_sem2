package com.example.safedispatch.service;


import com.example.safedispatch.model.Admin;
import com.example.safedispatch.repository.AdminRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetails implements UserDetailsService {
    private final AdminRepository adminRepo; //Admin repository for interacting with the database

    public UserDetails(AdminRepository adminRepo) { //Initialises the admin repository
        this.adminRepo = adminRepo;
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException { //Loads an admin by username
        Admin admin = adminRepo.findByUsername(username);
        if (admin == null) { //If the admin is not found, throw an error
            throw new UsernameNotFoundException("User not found");
        }
        System.out.println(admin.getUsername());
        return org.springframework.security.core.userdetails.User.withUsername(admin.getUsername()).password(admin.getPassword()).roles(admin.getRole()).build(); //Returns a User object
    }
}