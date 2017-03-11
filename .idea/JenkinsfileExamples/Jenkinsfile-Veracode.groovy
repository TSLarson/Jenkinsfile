node {
    try {
        checkout scm
        docker.image(env.DOCKER_REGISTRY + '/compozed/ci-base:0.6').inside {
            withCredentials([[
                                     $class          : 'UsernamePasswordMultiBinding',
                                     credentialsId   : 'VERACODE_INFO',
                                     passwordVariable: 'VERACODE_SECRET',
                                     usernameVariable: 'VERACODE_ID'
                             ]]) {
                stage('build') //you must first build your app
                        {
                            Client: {
                                ClientInstallDependencies: {
                                    sh '''cd client
                                npm config set strict-ssl false 
                                npm config set registry http://registry.npmjs.org 
                                npm config set proxy http://webproxy.igslb.allstate.com:8080 
                                npm cache clean
                                npm install --no-optional
                                npm install webpack'''
                                }
                            }
                            Server: {
                                sh 'export GRADLE_USER_HOME=~/.gradle; ./gradlew server:assemble'
                            }
                        }
                stage('Veracode Scan') { //then upload it to veracode
                    Client: {
                        sh 'cd client; NODE_ENV=dev ./node_modules/.bin/webpack --config webpack.config.js'
                        sh 'export ENV_SPACE=dev; export GRADLE_USER_HOME=~/.gradle; ./gradlew client:dist'
                        veracode applicationName: 'boilerplate-client',
                                canFailJob: true,
                                createProfile: true,
                                createSandbox: true,
                                criticality: 'Medium',
                                debug: true,
                                copyRemoteFiles: true,
                                fileNamePattern: '',
                                useProxy: false,
                                replacementPattern: '',
                                sandboxName: '',
                                scanExcludesPattern: '',
                                scanIncludesPattern: '',
                                scanName: '$buildnumber-$timestamp',
                                uploadExcludesPattern: '',
                                uploadIncludesPattern: 'client/build/distributions/**.zip',
                                useIDkey: true,
                                vid: env.VERACODE_ID,
                                vkey: env.VERACODE_SECRET
                    }
                    Server: {
                        veracode applicationName: 'boilerplate-server',
                                canFailJob: true,
                                createProfile: true,
                                createSandbox: true,
                                criticality: 'Medium',
                                debug: true,
                                copyRemoteFiles: true,
                                fileNamePattern: '',
                                useProxy: false,
                                replacementPattern: '',
                                sandboxName: '',
                                scanExcludesPattern: '',
                                scanIncludesPattern: '',
                                scanName: '$buildnumber-$timestamp',
                                uploadExcludesPattern: '',
                                uploadIncludesPattern: 'server/build/libs/**.jar',
                                useIDkey: true,
                                vid: env.VERACODE_ID,
                                vkey: env.VERACODE_SECRET
                    }
                }
            }
        }
    }
    finally { //runs at the end if successful or unsuccessful
        stage('Cleanup WS') {
            // You must clean up this workspace or specify the exact artifact name
            step([$class: 'WsCleanup'])
        }
    }
}