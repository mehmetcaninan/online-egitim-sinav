#!/bin/bash

# CI/CD Pipeline Başlatma Scripti
# Bu script tüm pipeline'ı yerel ortamda test etmek için kullanılır

echo "🚀 Online Eğitim Sınav Sistemi CI/CD Pipeline Başlatılıyor..."

# Renk kodları
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Hata kontrolü
set -e

# 1. Ortam Kontrolü
echo -e "${YELLOW}1. Ortam kontrolleri yapılıyor...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java bulunamadı. Java 17+ gerekli.${NC}"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker bulunamadı. Docker gerekli.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Ortam kontrolleri başarılı${NC}"

# 2. Temizlik ve Hazırlık
echo -e "${YELLOW}2. Temizlik ve hazırlık işlemleri...${NC}"
./scripts/jenkins-helper.sh full-cleanup || echo "Helper script bulunamadı, manuel temizlik yapılıyor..."

# Maven temizlik
./mvnw clean

echo -e "${GREEN}✅ Temizlik tamamlandı${NC}"

# 3. Build İşlemi
echo -e "${YELLOW}3. Build işlemi başlatılıyor...${NC}"
./mvnw compile
echo -e "${GREEN}✅ Build başarılı${NC}"

# 4. Birim Testleri
echo -e "${YELLOW}4. Birim testleri çalıştırılıyor...${NC}"
./mvnw test
TEST_RESULT_UNIT=$?

if [ $TEST_RESULT_UNIT -eq 0 ]; then
    echo -e "${GREEN}✅ Birim testleri başarılı${NC}"
else
    echo -e "${RED}❌ Birim testleri başarısız${NC}"
    echo "Test raporları: target/surefire-reports/"
fi

# 5. Entegrasyon Testleri
echo -e "${YELLOW}5. Entegrasyon testleri çalıştırılıyor...${NC}"
./mvnw verify
TEST_RESULT_INTEGRATION=$?

if [ $TEST_RESULT_INTEGRATION -eq 0 ]; then
    echo -e "${GREEN}✅ Entegrasyon testleri başarılı${NC}"
else
    echo -e "${RED}❌ Entegrasyon testleri başarısız${NC}"
    echo "Test raporları: target/failsafe-reports/"
fi

# 6. Docker Image Oluşturma
echo -e "${YELLOW}6. Docker image oluşturuluyor...${NC}"
./mvnw spring-boot:build-image || ./mvnw jib:dockerBuild
echo -e "${GREEN}✅ Docker image oluşturuldu${NC}"

# 7. Container Başlatma
echo -e "${YELLOW}7. Test container'ı başlatılıyor...${NC}"
docker stop online-egitim-test 2>/dev/null || true
docker rm online-egitim-test 2>/dev/null || true

docker run -d --name online-egitim-test \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=test \
    online-egitim-sinav:0.0.1-SNAPSHOT

# Uygulamanın başlamasını bekle
echo "Uygulamanın başlaması bekleniyor..."
sleep 30

# Sağlık kontrolü
for i in {1..10}; do
    if curl -f -s http://localhost:8080 > /dev/null; then
        echo -e "${GREEN}✅ Uygulama çalışıyor${NC}"
        break
    else
        echo "Uygulama henüz hazır değil... ($i/10)"
        sleep 5
    fi

    if [ $i -eq 10 ]; then
        echo -e "${RED}❌ Uygulama başlatılamadı${NC}"
        docker logs online-egitim-test
        exit 1
    fi
done

# 8. Selenium Testleri
echo -e "${YELLOW}8. Selenium testleri çalıştırılıyor...${NC}"

# Chrome driver için headless mod ayarla
export SELENIUM_HEADLESS=true

# Her test senaryosunu ayrı ayrı çalıştır
SELENIUM_TESTS=(
    "UserLoginSeleniumTest"
    "ExamCreationSeleniumTest"
    "ExamTakingSeleniumTest"
    "ResultViewSeleniumTest"
    "UserProfileSeleniumTest"
    "AdminPanelSeleniumTest"
    "QuestionManagementSeleniumTest"
    "ReportViewSeleniumTest"
    "FileUploadSeleniumTest"
    "PerformanceSeleniumTest"
)

SELENIUM_RESULTS=()

for test in "${SELENIUM_TESTS[@]}"; do
    echo -e "${YELLOW}   🧪 $test çalıştırılıyor...${NC}"

    if ./mvnw test -Dtest=$test; then
        echo -e "${GREEN}   ✅ $test başarılı${NC}"
        SELENIUM_RESULTS+=("$test:PASS")
    else
        echo -e "${RED}   ❌ $test başarısız${NC}"
        SELENIUM_RESULTS+=("$test:FAIL")
    fi
done

# 9. Temizlik
echo -e "${YELLOW}9. Temizlik işlemleri...${NC}"
docker stop online-egitim-test 2>/dev/null || true
docker rm online-egitim-test 2>/dev/null || true
echo -e "${GREEN}✅ Temizlik tamamlandı${NC}"

# 10. Sonuç Raporu
echo ""
echo "=========================="
echo "🏁 CI/CD PIPELINE RAPORU"
echo "=========================="
echo ""

if [ $TEST_RESULT_UNIT -eq 0 ]; then
    echo -e "Birim Testleri: ${GREEN}✅ BAŞARILI${NC}"
else
    echo -e "Birim Testleri: ${RED}❌ BAŞARISIZ${NC}"
fi

if [ $TEST_RESULT_INTEGRATION -eq 0 ]; then
    echo -e "Entegrasyon Testleri: ${GREEN}✅ BAŞARILI${NC}"
else
    echo -e "Entegrasyon Testleri: ${RED}❌ BAŞARISIZ${NC}"
fi

echo ""
echo "Selenium Test Sonuçları:"
for result in "${SELENIUM_RESULTS[@]}"; do
    test_name=$(echo $result | cut -d: -f1)
    test_result=$(echo $result | cut -d: -f2)

    if [ "$test_result" = "PASS" ]; then
        echo -e "  $test_name: ${GREEN}✅ BAŞARILI${NC}"
    else
        echo -e "  $test_name: ${RED}❌ BAŞARISIZ${NC}"
    fi
done

echo ""
echo "Test Raporları:"
echo "  - Birim testleri: target/surefire-reports/"
echo "  - Entegrasyon testleri: target/failsafe-reports/"
echo "  - Selenium testleri: target/selenium-reports/"
echo ""

# Genel başarı durumu
TOTAL_FAILED=0
if [ $TEST_RESULT_UNIT -ne 0 ]; then
    TOTAL_FAILED=$((TOTAL_FAILED + 1))
fi

if [ $TEST_RESULT_INTEGRATION -ne 0 ]; then
    TOTAL_FAILED=$((TOTAL_FAILED + 1))
fi

for result in "${SELENIUM_RESULTS[@]}"; do
    test_result=$(echo $result | cut -d: -f2)
    if [ "$test_result" = "FAIL" ]; then
        TOTAL_FAILED=$((TOTAL_FAILED + 1))
    fi
done

if [ $TOTAL_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 TÜM TESTLER BAŞARILI! Pipeline tamamlandı.${NC}"
    exit 0
else
    echo -e "${RED}❌ $TOTAL_FAILED test(ler) başarısız oldu.${NC}"
    exit 1
fi
