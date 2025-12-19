pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        DOCKER_IMAGE = 'online-egitim-sinav'
        DOCKER_TAG = "${BUILD_NUMBER}"
        // Port çakışmasını önlemek için
        APP_PORT = '8081'
        SELENIUM_HUB_PORT = '4444'
    }

    stages {
        stage('1. GitHub Kodlarını Çek') {
            steps {
                echo 'GitHub\'dan kodlar çekiliyor...'
                checkout scm
            }
        }

        stage('2. Build İşlemi') {
            steps {
                echo 'Maven ile build işlemi başlatılıyor...'
                sh './mvnw clean compile'
                echo '✅ Build işlemi tamamlandı'
            }
        }

        stage('3. Birim Testleri') {
            steps {
                echo 'Birim testleri çalıştırılıyor...'
                sh './mvnw test'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    echo '📊 Birim test raporları yayınlandı'
                }
            }
        }

        stage('4. Entegrasyon Testleri') {
            steps {
                echo 'Entegrasyon testleri çalıştırılıyor...'
                sh './mvnw verify'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/failsafe-reports/*.xml'
                    echo '📊 Entegrasyon test raporları yayınlandı'
                }
            }
        }

        stage('5. Docker Container Oluştur ve Çalıştır') {
            steps {
                echo 'Docker image oluşturuluyor...'
                // Jib ile image build et (Dockerfile'dan daha hızlı)
                sh './mvnw jib:dockerBuild'

                echo 'Eski container\'lar durduruluyor...'
                sh '''
                    docker stop online-egitim-test || true
                    docker rm online-egitim-test || true
                '''

                echo 'Yeni container başlatılıyor...'
                sh '''
                    docker run -d --name online-egitim-test \
                        -p ${APP_PORT}:8081 \
                        -e SPRING_PROFILES_ACTIVE=test \
                        -e SERVER_PORT=8081 \
                        ${DOCKER_IMAGE}:${DOCKER_TAG}
                '''

                echo 'Uygulamanın başlaması bekleniyor...'
                // Health check ile uygulama hazır mı kontrol et
                sh '''
                    for i in {1..30}; do
                        if curl -f -s http://localhost:${APP_PORT}/actuator/health > /dev/null; then
                            echo "✅ Uygulama hazır!"
                            break
                        fi
                        echo "⏳ Uygulama başlatılıyor... ($i/30)"
                        sleep 3
                    done
                '''
            }
        }

        // Selenium test stage'lerini paralel çalıştır (performans için)
        stage('6. Selenium Test Senaryoları') {
            parallel {
                stage('6A. Kullanıcı Girişi Testi') {
                    steps {
                        echo '🧪 Test Senaryosu 1: Kullanıcı Giriş Testi'
                        sh 'mvn test -Dtest=UserLoginSeleniumTest -DbaseUrl=http://localhost:${APP_PORT}'
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'target/selenium-reports/login-test.xml'
                        }
                    }
                }

                stage('6B. Sınav Oluşturma Testi') {
                    steps {
                        echo '🧪 Test Senaryosu 2: Sınav Oluşturma Testi'
                        sh 'mvn test -Dtest=ExamCreationSeleniumTest -DbaseUrl=http://localhost:${APP_PORT}'
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'target/selenium-reports/exam-creation-test.xml'
                        }
                    }
                }

                stage('6C. Sınav Alma Testi') {
                    steps {
                        echo '🧪 Test Senaryosu 3: Sınav Alma Testi'
                        sh 'mvn test -Dtest=ExamTakingSeleniumTest -DbaseUrl=http://localhost:${APP_PORT}'
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'target/selenium-reports/exam-taking-test.xml'
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline tamamlandı, temizlik yapılıyor...'
            sh '''
                docker stop online-egitim-test || true
                docker rm online-egitim-test || true
                docker image prune -f || true
            '''

            // Tüm test raporlarını birleştir ve yayınla
            publishHTML([
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'Code Coverage Report'
            ])
        }
        success {
            echo '✅ Tüm aşamalar başarıyla tamamlandı!'
            // Slack/Email notification gönderebilirsiniz
        }
        failure {
            echo '❌ Pipeline başarısız oldu!'
            // Hata detaylarını log'layın
            sh 'docker logs online-egitim-test || true'
        }
    }
}
