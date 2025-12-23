pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        DOCKER_IMAGE = 'online-egitim-sinav'
        DOCKER_TAG = "${BUILD_NUMBER}"
        APP_PORT = '8081'
    }

    stages {
        stage('1. Git Pull') {
            steps {
                echo '🔄 Pulling code from GitHub...'
                checkout scm
            }
        }

        stage('2. Build') {
            steps {
                echo '🔨 Building project...'
                sh './mvnw clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('3. Unit Tests') {
            steps {
                echo '🧪 Running unit tests...'
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('4. Integration Tests') {
            steps {
                echo '🔗 Running integration tests...'
                sh './mvnw verify -DskipUnitTests'
            }
            post {
                always {
                    junit 'target/failsafe-reports/*.xml'
                }
            }
        }

        stage('5. Docker Build & Run') {
            steps {
                echo '🐳 Building Docker image...'
                sh """
                    docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                """

                echo '🚀 Running container...'
                sh """
                    docker stop online-egitim-test || true
                    docker rm online-egitim-test || true
                    docker run -d --name online-egitim-test \
                        -p ${APP_PORT}:8081 \
                        -e SPRING_PROFILES_ACTIVE=test \
                        -e SERVER_PORT=8081 \
                        ${DOCKER_IMAGE}:${DOCKER_TAG}
                """

                echo '⏳ Waiting for app to be healthy...'
                sh '''
                    for i in {1..30}; do
                        if curl -f -s http://localhost:${APP_PORT}/actuator/health > /dev/null 2>&1; then
                            echo "✅ App up!"
                            exit 0
                        fi
                        sleep 3
                    done
                    echo "❌ App did not start!"
                    docker logs online-egitim-test
                    exit 1
                '''
            }
        }

        stage('6A. Selenium - Login Test') {
            steps {
                echo '🔵 Selenium: Login Test'
                sh './mvnw test -Dtest=UserLoginSeleniumTest -DbaseUrl=http://localhost:${APP_PORT}'
            }
        }
        stage('6B. Selenium - Exam Creation Test') {
            steps {
                echo '🔵 Selenium: Exam Creation Test'
                sh './mvnw test -Dtest=ExamCreationSeleniumTest -DbaseUrl=http://localhost:${APP_PORT}'
            }
        }
        stage('6C. Selenium - Exam Taking Test') {
            steps {
                echo '🔵 Selenium: Exam Taking Test'
                sh './mvnw test -Dtest=ExamTakingSeleniumTest -DbaseUrl=http://localhost:${APP_PORT}'
            }
        }

        // Örnek ekstra test (isteğe bağlı, puan artışı)
        stage('6D. Selenium - Optional Example') {
            when {
                expression { return true }
            }
            steps {
                echo '🔵 Optional Test'
                sh './mvnw test -Dtest=OptionalSeleniumTest -DbaseUrl=http://localhost:${APP_PORT}'
            }
        }
    }

    post {
        always {
            echo '📊 Cleaning and publishing results...'
            sh """
                docker stop online-egitim-test || true
                docker rm online-egitim-test || true
                docker image prune -f || true
            """
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/surefire-reports',
                reportFiles: 'index.html',
                reportName: 'Selenium Reports'
            ])
        }
        success {
            echo '✅ All stages completed successfully!'
        }
        unstable {
            echo '⚠️ Completed with unstable tests!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
