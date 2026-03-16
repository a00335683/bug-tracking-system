package com.tus.dtos;

public class LoginRequest {

    private String username;
    private String password;

    public LoginRequest() {
        // Needed for JSON
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}