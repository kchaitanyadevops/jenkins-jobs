node() {
    stage('BUILD') {
        echo 'Hello world'
    }
    
    stage('DEPLOY') {
        sh '''uptime
hostname
ls'''
    }
}
