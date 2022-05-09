void call(){
  stage('Build and push image') {
    steps {
        git branch: 'master', url: "${CODE_REPO}", credentialsId: 'github-weecover-miquel'
        script {
            GIT_COMMIT_SHORT = sh(
                    script: "printf \$(git rev-parse --short HEAD)",
                    returnStdout: true
            )
            // Reading POM version
            POM = readMavenPom file: 'pom.xml'
            POM.version = POM.version + "-" + GIT_COMMIT_SHORT
        }
        withMaven(
            maven: 'Maven3.6.3',
        ) {
            sh "mvn versions:set -DnewVersion=${POM.version}"
            withCredentials([usernamePassword(credentialsId: 'nexus-server', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
            {
                sh "mvn clean verify -Ddocker.image.repository=${CONTAINER_REGISTRY} -Djib.execution.goal=build -Djib.to.auth.username=${USERNAME} -Djib.to.auth.password=${PASSWORD}"
            }
        }
    }
  }
}
