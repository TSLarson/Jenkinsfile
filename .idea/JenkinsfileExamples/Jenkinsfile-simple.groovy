node {
    checkout scm
    stage('test/distribute')
            {
                docker.image(env.DOCKER_REGISTRY + '/compozed/ci-base:0.6').inside {
                    withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                                      credentialsId   : 'AVC-ADMIN',
                                      passwordVariable: 'ARTIFACTORY_PASSWORD',
                                      usernameVariable: 'ARTIFACTORY_USER'
                                     ]]) {
                        Client:
                        {
                            sh 'cd client && \
                                npm install --no-optional &&\
                                npm install webpack'
                            sh 'cd client; npm test'
                            sh 'cd client; ./node_modules/.bin/webpack --config webpack.config.js'
                            sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew client:publish'
                        }
                        Server:
                        {
                            sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew clean server:integrationTest -Penv=ci'
                            sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew clean server:test'
                            sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew server:publish -x server:test'
                        }
                    }
                }
            }
}
checkpoint "Before Dev Deployment"

node {
    stage('deploy dev')
            {
                docker.image(env.DOCKER_REGISTRY + '/compozed/ci-base:0.6').inside {
                    withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                                      credentialsId   : 'AVC-ADMIN',
                                      passwordVariable: 'CF_PASSWORD',
                                      usernameVariable: 'CF_USER'
                                     ]])
                            {
                                Client:
                                {
                                    sh 'export ENV_SPACE=dev; export GRADLE_USER_HOME=~/.gradle; ./gradlew client:fetchDeployableArtifact'
                                    sh 'export ENV_SPACE=dev; export GRADLE_USER_HOME=~/.gradle; ./gradlew client:cfPush -Penv=dev'
                                    sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew client:cfStart -Penv=dev'
                                }
                                Server:
                                {
                                    sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew server:fetchDeployableArtifact'
                                    sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew server:cfPush -Penv=dev'
                                    sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew server:cfStart -Penv=dev'
                                }
                            }
                }
            }
}
checkpoint "Before UAT Deployment"
stage('confirm uat deploy')
        {
            input 'Deploy to UAT?'
        }
node {
    stage('deploy uat')
            {
                docker.image(env.DOCKER_REGISTRY + '/compozed/ci-base:0.6').inside {
                    withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                                      credentialsId   : 'AVC-ADMIN',
                                      passwordVariable: 'CF_PASSWORD',
                                      usernameVariable: 'CF_USER'
                                     ]])
                            {
                                Client:
                                {
                                    sh 'export ENV_SPACE=uat; export GRADLE_USER_HOME=~/.gradle; ./gradlew client:fetchDeployableArtifact'
                                    sh 'export ENV_SPACE=uat; export GRADLE_USER_HOME=~/.gradle; ./gradlew client:cfPush -Penv=uat'
                                    sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew client:cfStart -Penv=uat'
                                }
                                Server:
                                {
                                    sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew server:fetchDeployableArtifact'
                                    sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew server:cfPush -Penv=uat'
                                    sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew server:cfStart -Penv=uat'
                                }
                            }
                }
            }
}