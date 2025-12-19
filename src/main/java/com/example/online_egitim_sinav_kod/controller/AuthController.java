package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.model.Role;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.service.UserService;
import com.example.online_egitim_sinav_kod.dto.LoginRequest;
import com.example.online_egitim_sinav_kod.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Validation
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Kullanıcı adı gereklidir"));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Şifre gereklidir"));
            }

            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ad soyad gereklidir"));
            }

            // Kullanıcı adı kontrolü
            if (userService.existsByUsername(request.getUsername().trim())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bu kullanıcı adı zaten kullanılıyor"));
            }

            User user = userService.createUser(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Kayıt başarılı! Admin onayını bekleyin.");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole(),
                "approved", true
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Kayıt sırasında hata oluştu: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Validation
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Kullanıcı adı gereklidir"));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Şifre gereklidir"));
            }

            // Debug logging
            String username = request.getUsername().trim();
            String password = request.getPassword().trim();
            System.out.println("LOGIN DEBUG: Attempting login for username: " + username);

            User user = userService.authenticate(username, password);

            if (user == null) {
                System.out.println("LOGIN DEBUG: Authentication failed for username: " + username);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Geçersiz kullanıcı adı veya şifre"));
            }

            // Reddedilmiş kullanıcı kontrolü
            if (user.isRejected()) {
                System.out.println("LOGIN DEBUG: User rejected: " + username);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Hesabınız admin tarafından reddedilmiştir. Lütfen yöneticiye başvurun."));
            }

            // Admin onayı kontrolü (admin hariç)
            if (!user.getRole().equals(Role.ADMIN) && !user.isApproved()) {
                System.out.println("LOGIN DEBUG: User not approved: " + username);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Hesabınız henüz admin tarafından onaylanmamış. Lütfen admin ile iletişime geçin."));
            }

            System.out.println("LOGIN DEBUG: Authentication successful for username: " + username);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Giriş başarılı");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole().toString(),
                "approved", user.isApproved()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("LOGIN DEBUG: Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Giriş sırasında hata oluştu: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole().toString()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Kullanıcı bilgileri alınamadı: " + e.getMessage()));
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Auth service is running"));
    }
}
