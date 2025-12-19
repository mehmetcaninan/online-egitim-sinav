package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
    List<User> findByApproved(boolean approved);
    boolean existsByUsername(String username);
}
