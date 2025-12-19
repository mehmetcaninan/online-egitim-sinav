# GITHUB VE JENKINS ENTEGRASYONU REHBERİ

## 🐙 ADIM 1: GITHUB REPOSITORY OLUŞTURMA

### 1.1 GitHub'da Yeni Repository Oluşturun
1. GitHub.com'a gidin ve giriş yapın
2. "New repository" butonuna tıklayın
3. Repository adı: `online-egitim-sinav-sistemi`
4. Public/Private seçin (Public öneriyorum)
5. "Create repository" butonuna tıklayın

### 1.2 Yerel Projeyi GitHub'a Bağlayın
```bash
# Proje dizininde terminal açın
cd "/Users/mehmetcaninan/Desktop/egitim sinav yedek 13 aralık test version"

# Git initialize edin
git init

# Tüm dosyaları ekleyin
git add .

# İlk commit'i yapın
git commit -m "Initial commit: Online Egitim Sinav Sistemi with CI/CD Pipeline"

# GitHub repository'yi remote olarak ekleyin (kendi username'inizi yazın)
git remote add origin https://github.com/KULLANICI-ADINIZ/online-egitim-sinav-sistemi.git

# Ana branch'i push edin
git branch -M main
git push -u origin main
```

## 🔧 ADIM 2: JENKINS KURULUMU VE AYARLARI

### 2.1 Jenkins Başlatma
```bash
# Jenkins container'ını başlatın
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17

# Jenkins'in başlamasını bekleyin (2-3 dakika)
# http://localhost:8080 adresini tarayıcıda açın
```

### 2.2 Jenkins İlk Kurulumu
```bash
# Jenkins şifresini alın
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Bu şifreyi kopyalayın ve tarayıcıdaki Jenkins'te girin
```

**Jenkins Kurulum Adımları:**
1. "Install suggested plugins" seçin
2. Admin kullanıcısı oluşturun
3. Jenkins URL'i varsayılan bırakın (http://localhost:8080/)

### 2.3 Gerekli Plugin'leri Kurun
Jenkins Dashboard'da:
1. "Manage Jenkins" → "Plugins"
2. "Available plugins" sekmesi
3. Şu plugin'leri arayın ve kurun:
   - Git Plugin ✅
   - Pipeline Plugin ✅ 
   - Maven Integration Plugin ✅
   - Docker Pipeline Plugin ✅
   - TestNG Results Plugin ✅
   - JUnit Plugin ✅
   - HTML Publisher Plugin ✅

## 🔗 ADIM 3: JENKINS JOB OLUŞTURMA

### 3.1 Pipeline Job Oluşturun
1. Jenkins Dashboard'da "New Item"
2. Job adı: `online-egitim-cicd-pipeline`
3. "Pipeline" tipini seçin
4. "OK" butonuna tıklayın

### 3.2 Pipeline Konfigürasyonu
**Pipeline** bölümünde:
- Definition: "Pipeline script from SCM"
- SCM: "Git"
- Repository URL: `https://github.com/KULLANICI-ADINIZ/online-egitim-sinav-sistemi.git`
- Branch: `*/main`
- Script Path: `Jenkinsfile`

**Build Triggers:**
- ☑️ "Poll SCM" seçin
- Schedule: `H/5 * * * *` (5 dakikada bir kontrol eder)

### 3.3 İlk Build Test
1. "Save" butonuna tıklayın
2. "Build Now" ile pipeline'ı test edin

## 🐳 ADIM 4: DOCKER AYARLARI

### 4.1 Docker Daemon'ın Jenkins'te Kullanımı
```bash
# Jenkins container'ında Docker CLI kurulması için:
docker exec -u root jenkins apt-get update
docker exec -u root jenkins apt-get install -y docker.io

# Docker socket permissions
sudo chmod 666 /var/run/docker.sock
```

### 4.2 Docker Compose Test
```bash
# Proje dizininde test edin
docker-compose up -d selenium-hub selenium-chrome
docker-compose down
```

## ⚙️ ADIM 5: JENKINS ENVIRONMENT VARIABLES

Jenkins Job konfigürasyonunda **Environment** bölümüne:
```
MAVEN_HOME=/usr/share/maven
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
DOCKER_HOST=unix:///var/run/docker.sock
```

## 🔐 ADIM 6: GITHUB WEBHOOK (Opsiyonel - Otomatik Build için)

### 6.1 GitHub Repository Ayarları
1. GitHub repository'nizde "Settings"
2. "Webhooks" → "Add webhook"
3. Payload URL: `http://YOUR-JENKINS-URL:8080/github-webhook/`
4. Content type: `application/json`
5. Events: "Just the push event"

## ✅ ADIM 7: TEST VE DOĞRULAMA

### 7.1 Pipeline Test
```bash
# Yerel test
chmod +x run-pipeline.sh
./run-pipeline.sh

# Jenkins'te test
# Dashboard'da job'u seçin ve "Build Now"
```

### 7.2 Port Kontrolü
```bash
# Hangi portların kullanıldığını kontrol edin
lsof -i :8080  # Jenkins
lsof -i :8081  # Test uygulaması
lsof -i :4444  # Selenium Hub
```

## 🚨 SORUN GİDERME

### Jenkins'e Erişemiyorum
```bash
docker logs jenkins
docker restart jenkins
```

### Port Çakışması
```bash
# 8080 portunu kullanan process'i bulun
lsof -i :8080
# Process'i durdurun veya farklı port kullanın
```

### Docker Permission Hatası
```bash
sudo usermod -aG docker jenkins
sudo chmod 666 /var/run/docker.sock
```
