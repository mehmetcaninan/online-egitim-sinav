#!/bin/bash
# GitHub'a Proje Yükleme Scripti

echo "🚀 Projeyi GitHub'a yüklüyor..."

# Kullanıcı bilgilerini alın
read -p "GitHub kullanıcı adınızı girin: " GITHUB_USERNAME
read -p "Repository adını girin (örn: online-egitim-sinav): " REPO_NAME

# Git konfigürasyonu
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Repository'yi initialize edin
git init
git add .
git commit -m "Initial commit: Online Eğitim Sınav Sistemi CI/CD Pipeline"

# GitHub'a bağlayın
git branch -M main
git remote add origin https://github.com/$GITHUB_USERNAME/$REPO_NAME.git

echo "✅ Git hazır. Şimdi GitHub'da repository oluşturun:"
echo "1. https://github.com/new adresine gidin"
echo "2. Repository name: $REPO_NAME"
echo "3. Public seçin"
echo "4. 'Create repository' tıklayın"
echo ""
echo "Repository oluşturduktan sonra enter'a basın..."
read -p "Repository oluşturdunuz mu? (Enter'a basın)"

# Push yapın
git push -u origin main

echo "🎉 Proje başarıyla GitHub'a yüklendi!"
echo "Repository URL: https://github.com/$GITHUB_USERNAME/$REPO_NAME"
