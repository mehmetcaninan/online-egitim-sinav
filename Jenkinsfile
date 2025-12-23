pipeline {
    agent any

    tools {
    jdk 'JDK17'
}

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean compile'
            }
        }

        stage('Unit Tests') {
            steps {
                sh './mvnw test -Dtest=*UnitTest'
            }
        }

        stage('Integration Tests') {
            when {
                expression { false } // şimdilik kapalı
            }
            steps {
                sh './mvnw test -Dtest=*IntegrationTest'
            }
        }

        stage('Selenium Tests') {
            when {
                expression { false } // şimdilik kapalı
            }
            steps {
                sh './mvnw test -Dtest=*SeleniumTest,*SeleniumIT'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
    }
}
