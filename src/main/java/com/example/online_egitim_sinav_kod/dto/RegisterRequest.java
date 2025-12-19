package com.example.online_egitim_sinav_kod.dto;

import com.example.online_egitim_sinav_kod.model.Role;

public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private Role requestedRole;

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String fullName, Role requestedRole) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.requestedRole = requestedRole;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRequestedRole() { return requestedRole; }
    public void setRequestedRole(Role requestedRole) { this.requestedRole = requestedRole; }
}
