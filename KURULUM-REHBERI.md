# KURULUM REHBERİ - Online Eğitim Sınav Sistemi CI/CD

## 📋 GEREKSİNİMLER

### 1. Java 17+ Kurulumu
```bash
# macOS (Homebrew ile)
brew install openjdk@17

# Java versiyonunu kontrol edin
java -version
javac -version
```

### 2. Maven Kurulumu
```bash
# macOS (Homebrew ile)
brew install maven

# Maven versiyonunu kontrol edin
mvn -version
```

### 3. Docker Kurulumu
```bash
# Docker Desktop'u indirin: https://www.docker.com/products/docker-desktop
# Kurulum sonrası test edin:
docker --version
docker-compose --version
```

### 4. Git Kurulumu
```bash
# macOS (Homebrew ile)
brew install git

# Git versiyonunu kontrol edin
git --version
```

### 5. Jenkins Kurulumu
```bash
# Docker ile Jenkins (önerilen)
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17

# Jenkins şifresini almak için:
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

## 🔍 PORT YÖNETİMİ

### Portlar:
- **Jenkins**: 8080 (varsayılan)
- **Uygulama (normal)**: 8080 
- **Uygulama (test)**: 8081 (çakışmayı önlemek için)
- **PostgreSQL**: 5432
- **Selenium Hub**: 4444

### ⚠️ ÖNEMLİ NOT
Test ortamında uygulama 8081 portunda çalışacak şekilde ayarladım. Bu sayıde Jenkins ile çakışma olmayacak.

## 📁 DOSYA YAPISI
Proje dizininizde şu dosyalar olmalı:
```
online-egitim-sinav-kod/
├── src/
├── target/
├── Jenkinsfile                 ✅ Hazır
├── Dockerfile                  ✅ Hazır  
├── docker-compose.yml          ✅ Hazır
├── pom.xml                     ✅ Güncelledim
├── run-pipeline.sh             ✅ Hazır
├── scripts/
│   └── jenkins-helper.sh       ✅ Hazır
└── README-CICD.md              ✅ Hazır
```

## ✅ KONTROL LİSTESİ
□ Java 17+ kurulu
□ Maven kurulu  
□ Docker kurulu ve çalışıyor
□ Git kurulu
□ Jenkins Docker container'ı çalışıyor
□ Proje dosyaları yerinde
□ GitHub repository oluşturuldu
