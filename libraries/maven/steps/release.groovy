void call(){
    POM = readMavenPom file: 'pom.xml'
    dir("deploy-code"){
        git branch: 'master', url: "${config.DEPLOY_REPO}", credentialsId: 'github-weecover'
        
        // Changing chart's version and appVersion
        chart_content = readYaml file: 'int/Chart.yaml'
        println "CURRENT CHART VERSION: ${chart_content.version}"
        println "CURRENT APP VERSION: ${chart_content.appVersion}"
        chart_content.appVersion = POM.version
        chart_content.version = POM.version
        writeYaml file: 'int/Chart.yaml', overwrite: true, data: chart_content
        
        // Changing microservice.version from values.yaml
        values_content = readYaml file: 'int/values.yaml'
        values_content.microservice.version=chart_content.appVersion
        writeYaml file: 'int/values.yaml', overwrite: true, data: values_content
        println "NEW APP VERSION: ${values_content.microservice.version}"
        
        // Commit and push
        sh "git config user.email 'jenkins' && git config user.name 'moliver-ext@aubay.es'"
        sh "git add int/Chart.yaml int/values.yaml && git commit -m 'Upgrade to appVersion ${chart_content.appVersion}. New Helm chart version: ${chart_content.version}'"
        withCredentials([gitUsernamePassword(credentialsId: 'github-weecover', gitToolName: 'git-tool')]) {
            sh "git push origin master"
        }
    }
}