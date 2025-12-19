package com.example.online_egitim_sinav_kod.service;

import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.model.Role;
import com.example.online_egitim_sinav_kod.repository.UserRepository;
import com.example.online_egitim_sinav_kod.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // Gerçek uygulamada şifre hash'lenmeli
        user.setFullName(request.getFullName());
        user.setRole(request.getRequestedRole());

        return userRepository.save(user);
    }

    public User authenticate(String username, String password) {
        return userRepository.findByUsername(username)
            .filter(user -> user.getPassword().equals(password)) // Gerçek uygulamada hash kontrolü yapılmalı
            .orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User findFirstByRole(String roleName) {
        try {
            Role role = Role.valueOf(roleName);
            List<User> users = userRepository.findByRole(role);
            return users.isEmpty() ? null : users.get(0);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public User updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setRole(role);
            return userRepository.save(user);
        }
        return null;
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
