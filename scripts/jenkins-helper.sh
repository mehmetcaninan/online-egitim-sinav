#!/bin/bash

# Jenkins CI/CD Pipeline Helper Script
# Bu script Jenkins pipeline'ında kullanılacak yardımcı işlevleri sağlar

set -e  # Hata durumunda scripti durdur

echo "🚀 CI/CD Pipeline Helper Script başlatıldı"

# Fonksiyon: Uygulamanın çalışıp çalışmadığını kontrol et
check_application_health() {
    local url=$1
    local max_attempts=30
    local attempt=1

    echo "⏳ Uygulama sağlığı kontrol ediliyor: $url"

    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null; then
            echo "✅ Uygulama çalışıyor! (Deneme: $attempt)"
            return 0
        else
            echo "⏳ Uygulama henüz hazır değil... (Deneme: $attempt/$max_attempts)"
            sleep 2
            ((attempt++))
        fi
    done

    echo "❌ Uygulama $max_attempts deneme sonrası hala yanıt vermiyor!"
    return 1
}

# Fonksiyon: Test raporlarını birleştir
merge_test_reports() {
    echo "📊 Test raporları birleştiriliyor..."

    mkdir -p target/combined-reports

    # Birim test raporları
    if [ -d "target/surefire-reports" ]; then
        cp target/surefire-reports/*.xml target/combined-reports/ 2>/dev/null || true
    fi

    # Entegrasyon test raporları
    if [ -d "target/failsafe-reports" ]; then
        cp target/failsafe-reports/*.xml target/combined-reports/ 2>/dev/null || true
    fi

    # Selenium test raporları
    if [ -d "target/selenium-reports" ]; then
        cp target/selenium-reports/*.xml target/combined-reports/ 2>/dev/null || true
    fi

    echo "✅ Test raporları birleştirildi"
}

# Fonksiyon: Docker container'ları temizle
cleanup_docker() {
    echo "🧹 Docker temizliği yapılıyor..."

    # Test container'larını durdur ve sil
    docker stop online-egitim-test 2>/dev/null || true
    docker rm online-egitim-test 2>/dev/null || true

    # Kullanılmayan image'ları temizle
    docker image prune -f 2>/dev/null || true

    echo "✅ Docker temizliği tamamlandı"
}

# Fonksiyon: Maven cache temizle
clean_maven_cache() {
    echo "🧹 Maven cache temizliği yapılıyor..."
    ./mvnw dependency:purge-local-repository -q || true
    echo "✅ Maven cache temizliği tamamlandı"
}

# Fonksiyon: Test ortamını hazırla
prepare_test_environment() {
    echo "🔧 Test ortamı hazırlanıyor..."

    # Test veritabanını temizle
    rm -rf target/test-db* 2>/dev/null || true

    # Test log dosyalarını temizle
    rm -rf logs/*.log 2>/dev/null || true

    # Test raporları dizinini oluştur
    mkdir -p target/selenium-reports
    mkdir -p target/test-reports

    echo "✅ Test ortamı hazırlandı"
}

# Fonksiyon: Selenium Grid'i başlat
start_selenium_grid() {
    echo "🌐 Selenium Grid başlatılıyor..."

    # Docker Compose ile Selenium Grid'i başlat
    if [ -f "docker-compose.yml" ]; then
        docker-compose up -d selenium-hub selenium-chrome

        # Hub'ın hazır olmasını bekle
        check_application_health "http://localhost:4444/wd/hub/status"

        echo "✅ Selenium Grid başlatıldı"
    else
        echo "⚠️ docker-compose.yml bulunamadı, Selenium Grid başlatılamadı"
    fi
}

# Fonksiyon: Selenium Grid'i durdur
stop_selenium_grid() {
    echo "🛑 Selenium Grid durduruluyor..."

    if [ -f "docker-compose.yml" ]; then
        docker-compose down selenium-hub selenium-chrome 2>/dev/null || true
        echo "✅ Selenium Grid durduruldu"
    fi
}

# Fonksiyon: Test sonuçlarını analiz et
analyze_test_results() {
    echo "📈 Test sonuçları analiz ediliyor..."

    local total_tests=0
    local passed_tests=0
    local failed_tests=0

    if [ -d "target/combined-reports" ]; then
        # XML raporlarından test sayılarını çıkar
        for report in target/combined-reports/*.xml; do
            if [ -f "$report" ]; then
                # Basit XML parsing (gerçek projede daha gelişmiş araçlar kullanın)
                tests=$(grep -o 'tests="[0-9]*"' "$report" | grep -o '[0-9]*' || echo "0")
                failures=$(grep -o 'failures="[0-9]*"' "$report" | grep -o '[0-9]*' || echo "0")

                total_tests=$((total_tests + tests))
                failed_tests=$((failed_tests + failures))
            fi
        done

        passed_tests=$((total_tests - failed_tests))

        echo "📊 Test Sonuçları:"
        echo "   Toplam: $total_tests"
        echo "   Başarılı: $passed_tests"
        echo "   Başarısız: $failed_tests"

        if [ $failed_tests -gt 0 ]; then
            echo "❌ Başarısız testler var!"
            return 1
        else
            echo "✅ Tüm testler başarılı!"
            return 0
        fi
    else
        echo "⚠️ Test raporları bulunamadı"
        return 1
    fi
}

# Komut satırı argümanlarını işle
case "$1" in
    "health-check")
        check_application_health "${2:-http://localhost:8080}"
        ;;
    "merge-reports")
        merge_test_reports
        ;;
    "cleanup")
        cleanup_docker
        clean_maven_cache
        ;;
    "prepare")
        prepare_test_environment
        ;;
    "start-selenium")
        start_selenium_grid
        ;;
    "stop-selenium")
        stop_selenium_grid
        ;;
    "analyze")
        analyze_test_results
        ;;
    "full-cleanup")
        cleanup_docker
        clean_maven_cache
        prepare_test_environment
        ;;
    *)
        echo "Kullanım: $0 {health-check|merge-reports|cleanup|prepare|start-selenium|stop-selenium|analyze|full-cleanup}"
        echo ""
        echo "Komutlar:"
        echo "  health-check [URL]  - Uygulamanın sağlığını kontrol et"
        echo "  merge-reports       - Test raporlarını birleştir"
        echo "  cleanup             - Docker ve Maven cache temizle"
        echo "  prepare             - Test ortamını hazırla"
        echo "  start-selenium      - Selenium Grid'i başlat"
        echo "  stop-selenium       - Selenium Grid'i durdur"
        echo "  analyze             - Test sonuçlarını analiz et"
        echo "  full-cleanup        - Tam temizlik yap"
        exit 1
        ;;
esac

echo "🏁 Script tamamlandı"
