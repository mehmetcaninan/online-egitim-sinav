-- H2 veritabanındaki Question tablosunu temizle
-- Eski field'lar yeni field'larla çakışıyor

-- Önce mevcut questions tablosunu kontrol et
SELECT COUNT(*) as total_questions FROM questions;

-- Tüm questions'ları sil (sınav sistemi yeni kurulduğu için güvenli)
DELETE FROM questions;

-- Veya alternatif olarak sadece sorunlu kayıtları sil
-- DELETE FROM questions WHERE text IS NULL OR text = '';

-- Sonuç kontrol
SELECT COUNT(*) as remaining_questions FROM questions;
