package com.tus.db.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String username;

    // Password will be stored encrypted (BCrypt)
    @Column(nullable = false)
    private String password;

    // Role defines permissions (ADMIN, DEVELOPER, TESTER)
    @Column(nullable = false)
    private String role;

    // Indicates if the account is active or disabled
    @Column(nullable = false)
    private boolean active = true;

    // Default constructor required by JPA
    public User() {}

    // Constructor used when creating users
    public User(String username, String password, String role, boolean active) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}