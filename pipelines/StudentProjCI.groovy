node {
    stage('Clone-Repo') {
        dir('CODE') {
            git 'https://github.com/citb30/studentapp-code.git'
        }
    }
    stage('Build'){
        sh "cd CODE ; mvn compile"
    }
    stage('Code-Quality') {
        sh "cd CODE ; mvn sonar:sonar -Dsonar.host.url=http://sonarqube:9000 -Dsonar.login=9fd9a621cffbfb18e3fa64e4455d151250a2deb3"
    }
    stage('Packaging') {
        sh 'cd CODE; mvn package'
    }
    stage('Dev Deploy') {
        build job: 'INSTANCE-CREATE', parameters: [string(name: 'PROJECT_NAME', value: 'student'), string(name: 'ENVIRONMENT', value: 'dev'), string(name: 'SERVER_NAME', value: 'studevapp01'), booleanParam(name: 'RECREATE', value: true)]
        dir('ANSIBLE') {
            git credentialsId: 'gitrouser', url: 'http://104.196.127.109/engineers/ansible.git'
        }
        sh '''
        cd ANSIBLE
echo 'studevapp01' >hosts 
ansible-playbook -i hosts -u ec2-user playbooks/proj-setup.yml
ansible-playbook -i hosts -u ec2-user playbooks/dev-deploy.yml
sleep 10
'''
    }
    stage('Selenium Testing') {
        dir('SELENIUM') {
            git 'https://github.com/citb30/selenium-sauce.git'
            sh '''
IPADDRESS=$(gcloud compute instances list | grep studevapp01 | awk '{print $(NF-1)}')
sed -i -e "s/IPADDRESS/$IPADDRESS/" src/test/java/framework/CrudStudent.java
mvn clean install "-Dremote=true" "-DseleniumGridURL=http://raghudevops30:51ece7b6-a740-43c6-90a6-a146b3727484@ondemand.saucelabs.com:80/wd/hub" "-Dplatform=Windows" "-Dbrowser=Chrome" "-Dbrowserversion=51" "-Doverwrite.binaries=true"
'''
        }
        
    }
    stage('API Testing'){
        dir('SELENIUM') {
            sh '''
IPADDRESS=$(gcloud compute instances list | grep studevapp01 | awk '{print $(NF-1)}')
python scripts/api-check.py $IPADDRESS
'''
        }
    }
    stage('Upload Artifacts') {
        dir('CODE') {
            sh '''
mvn versions:set -DnewVersion=$RELEASE_VERSION-RELEASE
mvn deploy
'''
        }
    }
 
} 
