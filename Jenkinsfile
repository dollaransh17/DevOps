pipeline {
    agent any
    tools { jdk 'jdk17'; maven 'maven3' }
    triggers { pollSCM('* * * * *') }
    options { timestamps(); disableConcurrentBuilds(); buildDiscarder(logRotator(numToKeepStr: '15')) }
    stages {
        stage('Checkout') { steps { checkout scm } }
        stage('Test') { steps { sh 'mvn -B clean test' } }
        stage('Package') { steps { sh 'mvn -B -DskipTests package' } }
        stage('Version artifact') {
            steps {
                sh 'mkdir -p target/artifacts; cp target/unit-converter.jar "target/artifacts/unit-converter-${BUILD_NUMBER}-$(git rev-parse --short HEAD).jar"'
            }
        }
    }
    post {
        always { junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true }
        success { archiveArtifacts artifacts: 'target/artifacts/*.jar', fingerprint: true }
    }
}
