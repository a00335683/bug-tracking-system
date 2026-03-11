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

            User tester = userRepository.findByUsername("tester1")
                    .orElse(new User());

            tester.setUsername("tester1");
            tester.setPassword(passwordEncoder.encode("pass"));
            tester.setRole("TESTER");
            tester.setActive(true);
            userRepository.save(tester);

            User developer = userRepository.findByUsername("dev1")
                    .orElse(new User());

            developer.setUsername("dev1");
            developer.setPassword(passwordEncoder.encode("pass"));
            developer.setRole("DEVELOPER");
            developer.setActive(true);
            userRepository.save(developer);
        };
    }
}