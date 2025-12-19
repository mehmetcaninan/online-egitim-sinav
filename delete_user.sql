-- Bu script belirtilen kullanıcıyı ve tüm ilişkili kayıtlarını siler
-- Kullanım: H2 Console'da (http://localhost:8080/h2-console) bu scripti çalıştırın

-- 1. Önce kullanıcıyı bulalım
SELECT id, username, full_name, role FROM users WHERE username LIKE '%est_user_1762787279456%';

-- 2. Kullanıcıya ait tüm CourseEnrollment kayıtlarını sil
DELETE FROM course_enrollments WHERE user_id IN (SELECT id FROM users WHERE username LIKE '%est_user_1762787279456%');

-- 3. Kullanıcıyı sınıflardan çıkar (classroom_students tablosundan)
DELETE FROM classroom_students WHERE student_id IN (SELECT id FROM users WHERE username LIKE '%est_user_1762787279456%');

-- 4. Eğer öğretmense, derslerini pasif yap (isteğe bağlı - şimdilik sadece silelim)
-- UPDATE courses SET active = false WHERE teacher_id IN (SELECT id FROM users WHERE username LIKE '%est_user_1762787279456%');

-- 5. Son olarak kullanıcıyı sil
DELETE FROM users WHERE username LIKE '%est_user_1762787279456%';

-- Kontrol: Kullanıcının silindiğini doğrula
SELECT id, username, full_name, role FROM users WHERE username LIKE '%est_user_1762787279456%';

