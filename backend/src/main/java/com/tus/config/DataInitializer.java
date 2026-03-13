package com.tus.config;

import com.tus.db.models.User;
import com.tus.db.repos.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(new User(
                        "admin",
                        passwordEncoder.encode("password"),
                        "ADMIN",
                        true
                ));
            }

            if (userRepository.findByUsername("tester1").isEmpty()) {
                userRepository.save(new User(
                        "tester1",
                        passwordEncoder.encode("pass"),
                        "TESTER",
                        true
                ));
            }

            if (userRepository.findByUsername("dev1").isEmpty()) {
                userRepository.save(new User(
                        "dev1",
                        passwordEncoder.encode("pass"),
                        "DEVELOPER",
                        true
                ));
            }
        };
    }
}