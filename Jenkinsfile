pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        sh './gradlew compileDebugSources'
      }
    }
    stage('Unit Test') {
      steps {
        sh './gradlew testDebugUnitTest testDebugUnitTest'
        junit '**/TEST-*.xml'
      }
    }
  }
}