package com.tus.config;

import com.tus.db.models.User;
import com.tus.db.repos.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User(
                        "admin",
                        passwordEncoder.encode("password"),
                        "ADMIN",
                        true
                );

                userRepository.save(admin);
            }
        };
    }
}