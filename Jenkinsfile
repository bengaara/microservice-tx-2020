//def branch_name = env.BRANCH_NAME.toString().split("/")[1]
def branch_name = BRANCH_NAME
def server = "10.22.28.64"; //getServer()

def getServer(){

  if(${branch_name} =='telkom'){
    return "secure.benkinet.com"
  }else if(${branch_name} =='development'){
    return "dev.tospay.net" //167.172.55.15
  }else if(${branch_name} =='production'){
    return "ukash.tospay.net"
  }else if(${branch_name} =='sandbox'){
    return "ukash.tospay.net"
  }else if(${branch_name} =='uat'){
    return "uat.tospay.net"//134.122.105.57
  }
    return server
}


pipeline {
    agent any
//     tools {
//            jdk 'jdk11'
//        }
    stages {

        stage('--dockerize-and-build-test-coverage--') {
            steps {
                // CD PROJECT FOLDER & BUILD JAR
                //sh "ls -la && mvn clean install -Dspring-boot.run.profiles=${branch_name}"
                // BUILD DOCKER IMAGE
                echo "docker-compose build ${branch_name}"
                sh "export DOCKER_BUILDKIT=1"
                 sh " docker-compose build  --no-cache --build-arg profile=${branch_name}"
            }
             post {
                always {
                //  junit 'target/**/*.xml'
                //  sh "docker-compose down -v"
                sh "docker ps "
                }
             }
        }
        stage('--deploy-to-registry--') {
            steps {

                // DEPLOY TO REGISTRY
                sh "docker login https://mjenzi.tospay.net -u tsp_registry -p tsp_registry && docker-compose push"

                 // CLEAR IMAGE
                 sh "docker-compose rm -f"

                 sh "docker system prune -f"
                 //remove all stopped
                //sh "docker container rm \$(docker container ls –aq)"
            }
        }

        stage('--Deploy to server uat--') {
                    when {
                        branch 'uat'
                    }
                   steps {
                          echo "deploy to server ${branch_name} " +  "134.122.105.57"
                           sh "chmod +x deploy.sh && ./deploy.sh ${branch_name} " + "134.122.105.57"
                          }
                   post {
                         failure {
                                  echo "failure : TODO send email"
                        //          mail to:"benson@tospay.net", subject: "Build Failed: ${currentBuild.fullDisplayName}",body: "This is the build ${env.BUILD_URL}"
                                  }
                         }
         }
        stage('--Deploy to server telkom--') {
                            when {
                                branch 'telkom'
                            }
                           steps {
                                  echo "deploy to server ${branch_name} " +  "secure.benkinet.com"
                                   sh "chmod +x deploy.sh && ./deploy.sh ${branch_name} " + "secure.benkinet.com"
                                  }
                           post {
                                 failure {
                                            echo "failure : TODO send email"
                                          //mail to: "benson@tospay.net", subject: "Build Failed: ${currentBuild.fullDisplayName}",body: "This is the build ${env.BUILD_URL}"
                                          }
                                 }
          }


    }
}



