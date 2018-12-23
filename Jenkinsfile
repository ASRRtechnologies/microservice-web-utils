pipeline {
    agent {
        docker {
            image 'maven:3-alpine' 
            args '-v maven-home:/root/.m2'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean install'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
        }
        stage('Checkstyle') {
            steps {
                sh 'mvn -B checkstyle:check'
            }
        }
    }
}
