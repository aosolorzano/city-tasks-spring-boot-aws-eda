
## Spring Boot Native/Reactive micro-service deployed on Fargate ECS in an Event-Driven Architecture with Lambda, SNS, and EventBridge.

* **Author**: [Andres Solorzano](https://www.linkedin.com/in/aosolorzano/).
* **Level**: Advanced.
* **Technologies**: Java 17, Spring Boot 3, Spring Native, Spring WebFlux, Spring OAuth2, Quartz, Flyway, AWS Copilot CLI, Testcontainers, LocalStack, Aurora Postgres, DynamoDB, Elastic Load Balancer (ELB), SNS, EventBridge, Lambda, and Docker.

![](utils/docs/images/solution_architecture_v3.png)

You can read the following sequence of articles to get more context:

1. [Multi-Account environment on AWS using IAM Identity Center](https://aosolorzano.medium.com/implementing-a-multi-account-environment-with-aws-organizations-and-the-iam-identity-center-d1cdb40bdf4d).
2. [OAuth2 in Spring Boot Native microservice](https://aosolorzano.medium.com/oauth2-in-spring-boot-native-reactive-microservice-with-amazon-cognito-as-oidc-service-c454d84a5234).
3. [Deploying Spring Boot Native microservice using Cross-Account deployment](https://aosolorzano.medium.com/spring-boot-native-microservice-on-ecs-fargate-using-aws-copilot-cli-for-cross-account-deployment-73b1836f21f7).
4. [End-to-End Encryption using TLS ECDSA certificate and ACM with Copilot CLI](https://aosolorzano.medium.com/end-to-end-encryption-using-tls-ecdsa-certificate-acm-and-aws-copilot-cli-64f5daafe977).

## Description.
This project uses Spring Boot to manage Quartz Jobs with the help of Spring Webflux and Spring Native.
The Quartz library is configured for a clustered environment, so it needs Postgres to store and manage Jobs executions.
When a Quartz Job is executed, the calling method retrieves the Device item associated with the Tasks and updates its state in DynamoDB.
To perform all these activities, the users must have a valid access token (JWT) to access the endpoints.
All test cases use TDD from the beginning of the development, and only Integration Tests are executed with the support of Testcontainers and LocalStack to try to cover real-world scenarios.
This project also uses Docker Compose to deploy a local cluster with the required services for local testing.

## Prerequisites.
- Git.
- AWS CLI (version 2.11.+)
- AWS Copilot CLI (version 1.27.+)
- OpenJDK (version 17.0. 
- Apache Maven (version 3.8.+)
- Spring Boot (version 3.0.7)
- Docker and Docker Compose.

## Deployment Options.
You need to execute the following command from the project's root directory:
```bash
./run-scripts.sh
```

The script will ask you for the required AWS profiles to deploy the application locally or in AWS:
![](utils/docs/images/bash_script_entering_variables.png)

Then, the script shows a main menu with the following options:
![](utils/docs/images/bash_script_main_menu.png)

Go to 'Helper menu' to create the required resources before deploying the application:

### Getting Device items from DynamoDB on LocalStack.
Execute the following command:
```bash
aws dynamodb scan                           \
  --table-name Devices                      \
  --endpoint-url http://localhost:4566
```

## Running Locally using Native Executable.
Use this option if you want to explore more features such as running your tests in a native image.
*IMPORTANT:* The GraalVM `native-image` compiler should be installed and configured on your machine.

Deploy the required services using Docker Compose command:
```bash
docker compose up localstack tasks-postgres tasks-proxy
```

Open a new terminal window and export the following environment variables:
```bash
export SPRING_PROFILES_ACTIVE=dev
export CITY_TASKS_DB_CLUSTER_SECRET='{"dbClusterIdentifier":"city-tasks-db-cluster","password":"postgres123","dbname":"CityTasksDB","engine":"postgres","port":5432,"host":"localhost","username":"postgres"}'
export CITY_IDP_ENDPOINT='https://cognito-idp.<your_cognito_region>.amazonaws.com/<your_cognito_user_pool_id>'
export CITY_TASKS_TIME_ZONE='-05:00'
export AWS_DEFAULT_REGION='ap-southeast-2'
export AWS_ACCESS_KEY_ID='DUMMY'
export AWS_SECRET_ACCESS_KEY='DUMMY'
export AWS_ENDPOINT_OVERRIDE='http://localhost:4566'
```

Then, create and run the native executable from the project's root directory:
```bash
$ ./mvnw clean native:compile -Pnative spring-boot:run
```

### AWS Copilot CLI - Helpful Commands.
List all of your AWS Copilot applications.
```bash
copilot app ls
```
Show information about the environments and services in your application.
```bash
copilot app show
```
Show information about your environments.
```bash
copilot env ls
```
List of all the services in an application.
```bash
copilot svc ls
```
Show service status.
```bash
copilot svc status
```
Show information about the service, including endpoints, capacity and related resources.
```bash
copilot svc show
```
Show logs of a deployed service.
```bash
copilot svc logs        \
    --app city-tasks    \
    --name api          \
    --env dev           \
    --since 1h          \
    --follow
```
Start an interactive bash session with a task part of the service:
```bash
copilot svc exec        \
    --app city-tasks    \
    --name api          \
    --env dev
```
To delete and clean-up all created resources.
```bash
copilot app delete --yes
```

## Spring Boot - Reference Documentation.
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.1.0/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.1.0/maven-plugin/reference/html/#build-image)
* [GraalVM Native Image Support](https://docs.spring.io/spring-boot/docs/3.1.0/reference/html/native-image.html#native-image)
* [Testcontainers](https://www.testcontainers.org/)
* [Testcontainers Postgres Module Reference Guide](https://www.testcontainers.org/modules/databases/postgres/)
* [Spring Reactive Web](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#web.reactive)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#data.sql.jpa-and-spring-data)
* [OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/)
* [Quartz Scheduler](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#io.quartz)
* [Flyway Migration](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#howto.data-initialization.migration-tool.flyway)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a Reactive RESTful Web Service](https://spring.io/guides/gs/reactive-rest-service/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Additional Links
These additional references should also help you:

* [Configure AOT settings in Build Plugin](https://docs.spring.io/spring-boot/docs/3.1.0/maven-plugin/reference/htmlsingle/#aot)


## GraalVM Native Support
This project has been configured to let you generate either a lightweight container or a native executable.
It is also possible to run your tests in a native image.

### Lightweight Container with Cloud Native Buildpacks
If you're already familiar with Spring Boot container images support, this is the easiest way to get started.
Docker should be installed and configured on your machine prior to creating the image.

To create the image, run the following goal:

```bash
$ ./mvnw spring-boot:build-image -Pnative -DskipTests
```

Then, you can run the app like any other container:

```bash
$ docker run --rm city-tasks-api:1.6.0
```

### Executable with Native Build Tools
Use this option if you want to explore more options such as running your tests in a native image.
The GraalVM `native-image` compiler should be installed and configured on your machine.

**NOTE:** GraalVM 22.3+ is required.

To create the executable, run the following goal:

```bash
$ ./mvnw native:compile -Pnative -DskipTests
```

Then, you can run the app as follows:
```bash
$ target/city-tasks-api
```

You can also run your existing tests suite in a native image.
This is an efficient way to validate the compatibility of your application.

To run your existing tests in a native image, run the following goal:

```bash
$ ./mvnw test -PnativeTest
```
