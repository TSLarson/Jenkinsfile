node {
    stage 'deploy staging'
    docker.image(env.DOCKER_REGISTRY + '/compozed/ci-base:0.6').inside {
        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : 'AVC-ADMIN',
                          passwordVariable: 'CF_PASSWORD',
                          usernameVariable: 'CF_USER'],
                         [$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : 'SPRING-DATASOURCE-STAGING',
                          passwordVariable: 'DATASOURCE_PASSWORD',
                          usernameVariable: 'DATASOURCE_USER'],
                         [$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : 'FLYWAY_STAGING',
                          passwordVariable: 'FLYWAY_PASSWORD',
                          usernameVariable: 'FLYWAY_USER'],
        ]) {
            Client:
            {
                // This calls the conveyor plugin
                step([$class      : 'ConveyorJenkinsPlugin', applicationName: "boilerplate-client-staging",
                      artifactURL : "https://artifactory.allstate.com/artifactory/libs-release/com/allstate/cts/boilerplate-client/1.0.${SOURCE_BUILD_NUMBER}-staging/boilerplate-client-1.0.${SOURCE_BUILD_NUMBER}-staging.zip",
                      environment : 'non-prod', manifest:
                              """applications:
- name: boilerplate-client-staging
  instances: 1
  memory: 1024M
  disk_quota: 1024M
  routes:
    - route: avc-app-staging.apps.nonprod-mpn.ro11.allstate.com
    - route: boilerplate-client-staging.auth-platform-sandbox.allstate.com
  env:
    AVC_BACKEND_URL: https://boilerplate-server-staging.apps.nonprod-mpn.ro11.allstate.com
  host: boilerplate-client-staging
  domain: auth-platform-sandbox.allstate.com""", \
                               organization: 'REPLACE_WITH_YOUR_CF_ORG', password: "${env.CF_PASSWORD}",
                      serviceNowGroup      : 'REPLACE_WITH_YOUR_CHANGE_GROUP',
                      serviceNowUserID: "${BUILD_USER_ID}",
                      space: 'STAGING',
                      username: "${env.CF_USER}"])
            }
            Server:
            {
                // This calls the conveyor plugin
                step([$class         : 'ConveyorJenkinsPlugin',
                      applicationName: "boilerplate-server-staging",
                      artifactURL    : "https://artifactory.allstate.com/artifactory/libs-release-local/com/allstate/cts/boilerplate-server/1.0.${SOURCE_BUILD_NUMBER}/boilerplate-server-1.0.${SOURCE_BUILD_NUMBER}.jar",
                      environment    : 'non-prod',
                      manifest       :
                              """applications:
- name: boilerplate-server
  instances: 1
  memory: 1024M
  disk_quota: 1024M
  routes:
    - route: boilerplate-server-staging.apps.nonprod-mpn.ro11.allstate.com
  env:
    FLYWAY_PASSWORD: ${env.FLYWAY_PASSWORD}
    FLYWAY_PLACEHOLDERS_DATA_TABLESPACE: A7SD010
    FLYWAY_PLACEHOLDERS_FLYWAY_USER: A7TESTS1
    FLYWAY_PLACEHOLDERS_INDEX_TABLESPACE: A7SI010
    FLYWAY_PLACEHOLDERS_LOB_TABLESPACE: A7SL010
    FLYWAY_PLACEHOLDERS_SPRING_DATASOURCE_USERNAME: A7TESTS1USR
    FLYWAY_URL: jdbc:oracle:thin:@ldap://oid.allstate.com:389/a7sod001,cn=OracleContext,dc=allstate,dc=com
    FLYWAY_USER: A7TESTS1
    SPRING_DATASOURCE_PASSWORD: ${env.DATASOURCE_PASSWORD}
    SPRING_DATASOURCE_URL: jdbc:oracle:thin:@ldap://oid.allstate.com:389/a7sod001,cn=OracleContext,dc=allstate,dc=com
    SPRING_DATASOURCE_USERNAME: A7TESTS1USR""",
                      organization   : 'REPLACE_WITH_YOUR_CF_ORG', password: "${env.CF_PASSWORD}",
                      serviceNowGroup: 'REPLACE_WITH_YOUR_CHANGE_GROUP', serviceNowUserID: "ck86k", space: 'STAGING', username: "${env.CF_USER}"])
            }
        }
    }
}