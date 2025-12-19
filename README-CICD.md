# Online Eğitim Sınav Sistemi - CI/CD Pipeline

Bu proje Jenkins kullanarak otomatik CI/CD pipeline'ına sahiptir. Kod üzerinde yapılan her değişiklik otomatik olarak test edilir ve deploy edilir.

## CI/CD Pipeline Aşamaları

### 1. GitHub'dan Kod Çekme
- Jenkins, GitHub repository'den en son kodları çeker
- Webhook ile otomatik tetiklenir

### 2. Build İşlemi
- Maven ile proje derlenir
- Dependency'ler indirilir
- JAR dosyası oluşturulur

### 3. Birim Testleri
- JUnit ve Spring Boot testleri çalıştırılır
- Test sonuçları raporlanır
- Code coverage analizi yapılır

### 4. Entegrasyon Testleri
- Spring Boot integration testleri
- Database bağlantı testleri
- API endpoint testleri

### 5. Docker Container Oluşturma
- Docker image build edilir
- Container olarak çalıştırılır
- Sağlık kontrolü yapılır

### 6. Selenium Test Senaryoları

#### 6A. Kullanıcı Giriş Testi
- Geçerli/geçersiz kullanıcı girişi
- Session yönetimi
- Güvenlik kontrolleri

#### 6B. Sınav Oluşturma Testi
- Admin paneli erişimi
- Sınav formu doldurma
- Validation kontrolleri

#### 6C. Sınav Alma Testi
- Öğrenci sınav alma süreci
- Soru gezinme
- Zaman yönetimi

#### 6D. Sonuç Görüntüleme Testi
- Sınav sonuçları sayfası
- Rapor görüntüleme
- İstatistik kontrolleri

#### 6E. Kullanıcı Profil Testi
- Profil güncelleme
- Şifre değiştirme
- Kişisel bilgi yönetimi

#### 6F. Admin Panel Testi
- Admin dashboard
- Kullanıcı yönetimi
- Sistem ayarları

#### 6G. Soru Yönetimi Testi
- Soru ekleme/düzenleme
- Toplu soru yükleme
- Kategori yönetimi

#### 6H. Rapor Görüntüleme Testi
- Detaylı raporlar
- Excel export
- Grafik görüntüleme

#### 6I. Dosya Yükleme Testi
- Soru dosyası yükleme
- Kaynak doküman yükleme
- Dosya validasyonu

#### 6J. Performans Testi
- Sayfa yükleme süreleri
- Çoklu kullanıcı testi
- Database performansı

## Kurulum ve Kullanım

### Gereksinimler
- Java 17+
- Maven 3.6+
- Docker
- Jenkins
- Chrome Browser (Selenium için)

### Jenkins Kurulumu

1. **Jenkins Plugin'leri:**
   ```
   - Git Plugin
   - Maven Integration Plugin
   - Docker Plugin
   - TestNG Results Plugin
   - JUnit Plugin
   - HTML Publisher Plugin
   ```

2. **Jenkins Job Oluşturma:**
   ```bash
   # Pipeline job oluşturun
   # Pipeline script from SCM seçin
   # Repository URL'i girin
   # Jenkinsfile path'i belirtin
   ```

3. **Jenkins Konfigürasyonu:**
   ```groovy
   // Jenkinsfile içeriği zaten hazır
   // Global tool configuration'da Maven ve JDK ayarlayın
   ```

### Yerel Çalıştırma

```bash
# Proje build et
./mvnw clean compile

# Birim testleri çalıştır
./mvnw test

# Entegrasyon testleri çalıştır
./mvnw verify

# Selenium testleri çalıştır
./mvnw test -Dtest=*SeleniumTest

# Docker ile çalıştır
docker-compose up -d

# Tüm pipeline'ı simüle et
./scripts/jenkins-helper.sh full-cleanup
./mvnw clean package
./scripts/jenkins-helper.sh start-selenium
./mvnw test -Dtest=*SeleniumTest
./scripts/jenkins-helper.sh stop-selenium
```

### Test Raporları

Test raporları şu dizinlerde oluşturulur:
- `target/surefire-reports/` - Birim testleri
- `target/failsafe-reports/` - Entegrasyon testleri
- `target/selenium-reports/` - Selenium testleri
- `target/combined-reports/` - Birleştirilmiş raporlar

### Docker Compose Kullanımı

```bash
# Tüm servisleri başlat
docker-compose up -d

# Sadece uygulama
docker-compose up app

# Selenium Grid ile
docker-compose up selenium-hub selenium-chrome

# Temizlik
docker-compose down
```

### Troubleshooting

#### Jenkins Pipeline Hataları
```bash
# Log'ları kontrol et
docker logs jenkins

# Workspace temizle
./scripts/jenkins-helper.sh full-cleanup
```

#### Selenium Test Hataları
```bash
# Chrome driver güncelle
./mvnw dependency:purge-local-repository
./mvnw clean test

# Selenium Grid durumunu kontrol et
curl http://localhost:4444/wd/hub/status
```

#### Docker Hataları
```bash
# Container'ları kontrol et
docker ps -a

# Image'ları temizle
docker system prune -f
```

## Performans Metrikleri

Pipeline her çalıştırıldığında şu metrikleri takip eder:
- Build süresi
- Test execution süresi
- Code coverage yüzdesi
- Test başarı oranı
- Deploy süresi

### SLA Hedefleri
- Build süresi: < 5 dakika
- Test execution: < 10 dakika
- Total pipeline: < 20 dakika
- Test success rate: > %95

## Güvenlik

- Hassas bilgiler environment variable olarak yönetilir
- Docker container'lar non-root user ile çalışır
- Database bağlantıları şifrelenmiş
- API endpoint'leri authentication gerektiriyor

## Monitoring

Jenkins dashboard'da şu bilgiler izlenir:
- Build başarı/başarısızlık oranları
- Test trend analizi
- Performance metrikleri
- Error log analizi

## Katkıda Bulunma

1. Feature branch oluşturun
2. Testlerinizi yazın
3. Pull request açın
4. CI/CD pipeline'ın başarıyla tamamlanmasını bekleyin

## İletişim

Sorularınız için lütfen development team ile iletişime geçin.
