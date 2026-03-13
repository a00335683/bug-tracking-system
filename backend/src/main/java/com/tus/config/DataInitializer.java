package com.tus.config;

import com.tus.db.models.User;
import com.tus.db.repos.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile({"dev", "test"})
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            userRepository.findByUsername("admin").ifPresent(userRepository::delete);
            userRepository.findByUsername("tester1").ifPresent(userRepository::delete);
            userRepository.findByUsername("dev1").ifPresent(userRepository::delete);

            userRepository.save(new User(
                    "admin",
                    passwordEncoder.encode("password"),
                    "ADMIN",
                    true
            ));

            userRepository.save(new User(
                    "tester1",
                    passwordEncoder.encode("tester123"),
                    "TESTER",
                    true
            ));

            userRepository.save(new User(
                    "dev1",
                    passwordEncoder.encode("pass"),
                    "DEVELOPER",
                    true
            ));
        };
    }
}