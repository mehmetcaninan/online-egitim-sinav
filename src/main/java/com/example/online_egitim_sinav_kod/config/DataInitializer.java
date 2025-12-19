package com.example.online_egitim_sinav_kod.config;

import com.example.online_egitim_sinav_kod.model.Role;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.repository.UserRepository;
import com.example.online_egitim_sinav_kod.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("default")
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final StorageService storageService;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(StorageService storageService, UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Initializing storage...");
            storageService.init();
            logger.info("Storage initialized at: {}", System.getProperty("user.dir") + "/filestorage");
        } catch (Exception e) {
            logger.error("Failed to initialize storage", e);
        }

        // Database migration: Add rejected column if it doesn't exist
        try {
            logger.info("Checking database schema for rejected column...");
            // Check if column exists by trying to query it
            try {
                jdbcTemplate.queryForObject("SELECT rejected FROM users LIMIT 1", Boolean.class);
                logger.info("Rejected column already exists");
            } catch (Exception e) {
                // Column doesn't exist, add it
                logger.info("Adding rejected column to users table...");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN rejected BOOLEAN DEFAULT false");
                logger.info("Database migration completed: rejected column added");
            }
        } catch (Exception e) {
            logger.error("Error during database migration: {}", e.getMessage());
        }

        try {
            logger.info("Checking admin user in database...");

            // Mevcut admin kullanıcısını kontrol et ve gerekirse güncelle
            User adminUser = userRepository.findByUsername("admin").orElse(null);

            if (adminUser != null) {
                // Eğer admin var ama şifresi hash'lenmiş ise güncelle
                if (!adminUser.getPassword().equals("123456")) {
                    logger.info("Updating admin password to plain text...");
                    adminUser.setPassword("123456");
                    adminUser.setApproved(true); // Admin her zaman onaylı
                    adminUser = userRepository.save(adminUser);
                    logger.info("Admin password updated to plain text");
                }
                // Admin'in onay durumunu kontrol et
                if (!adminUser.isApproved()) {
                    adminUser.setApproved(true);
                    adminUser = userRepository.save(adminUser);
                }
            } else {
                // Admin yoksa oluştur
                logger.info("Admin user not found, creating...");
                adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setFullName("System Administrator");
                adminUser.setRole(Role.ADMIN);
                adminUser.setPassword("123456");
                adminUser.setApproved(true); // Admin otomatik olarak onaylı
                adminUser = userRepository.save(adminUser);
                logger.info("Admin user created with id={}", adminUser.getId());
            }

            // Test öğretmeni oluştur (yoksa)
            User teacherUser = userRepository.findByUsername("ogretmen").orElse(null);
            if (teacherUser == null) {
                logger.info("Creating test teacher user...");
                teacherUser = new User();
                teacherUser.setUsername("ogretmen");
                teacherUser.setFullName("Test Öğretmen");
                teacherUser.setRole(Role.TEACHER);
                teacherUser.setPassword("123456");
                teacherUser.setApproved(true);
                teacherUser = userRepository.save(teacherUser);
                logger.info("Test teacher user created with id={}", teacherUser.getId());
            }

            // Test öğrencisi oluştur (yoksa)
            User studentUser = userRepository.findByUsername("ogrenci").orElse(null);
            if (studentUser == null) {
                logger.info("Creating test student user...");
                studentUser = new User();
                studentUser.setUsername("ogrenci");
                studentUser.setFullName("Test Öğrenci");
                studentUser.setRole(Role.STUDENT);
                studentUser.setPassword("123456");
                studentUser.setApproved(true);
                studentUser = userRepository.save(studentUser);
                logger.info("Test student user created with id={}", studentUser.getId());
            }

            logger.info("Data initialization completed. Admin: {}, Teacher: {}, Student: {}",
                       adminUser.getUsername(), teacherUser.getUsername(), studentUser.getUsername());

            // Veritabanı durumunu kontrol et
            long userCount = userRepository.count();
            logger.info("Total users in database: {}", userCount);

        } catch (Exception ex) {
            logger.error("Error while ensuring users exist", ex);
        }
    }
}
