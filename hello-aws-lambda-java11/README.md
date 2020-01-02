# AWS Lambda using Java with Java11 Runtime

This is an over-engineered "hello world" style AWS Lambda created using Java11 showing:
- deployment package prepared using either [Gradle](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#gradle) or [Maven](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#maven)
- handler function using either POJOs or Streams
- execution context
- function context
- usage of reserved environment variables
- usage of custom environment variables
- separation of worker function for unit testing
- logging using SLF4J and logback
- error handling
- tracing using AWS X-Ray
- useful AWS CLI commands for
  - [creating](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#create-function) AWS Lambda resources
  - [updating](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#update-function) AWS Lambda resources
  - [invoking](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#test-function) AWS Lambda resources
  - [getting](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#get-function) AWS Lambda resources
  - [listing](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#list-function) AWS Lambda resources
  - [deleting](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#delete-function) AWS Lambda resources

## Requirements
- [AWS Command Line Interface](https://aws.amazon.com/cli/)
- [Amazon Corretto JDK 11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
- AWS_ROLE_ARN - AWS resource name of the role used for the Lambda function. At a bare minimum, the role must be equivalent with attaching the following AWS managed policies:
  - AWSLambdaBasicExecutionRole - provides limited write access to AWS CloudWatch Logs 
  - AWSXrayWriteOnlyAccess - provides limited read write access to AWS X-Ray
    
## Start

### Test and Package the Function Code
Choose between either Gradle or Maven - there are wrapper scripts provided for both.
- Gradle
```shell
./gradlew clean && ./gradlew test && ./gradlew shadowJar
```
- Maven
```shell
./mvnw clean && ./mvnw test && ./mvnw package
```

The only difference between these 2 options is the location of the output fat jar:
- Gradle
```shell
build/libs/hello-aws-lambda-java11.jar
```
- Maven
```shell
target/hello-aws-lambda-java11.jar
```

### Create Function
Assuming the Gradle specific location of the fat jar and the POJOs function handler:
```shell
aws lambda create-function \
    --function-name hello-aws-lambda-java11 \
    --zip-file fileb://build/libs/hello-aws-lambda-java11.jar \
    --environment Variables={BUILD_AUTOMATION_SYSTEM=Gradle} \
    --role $(AWS_ROLE_ARN)  \
    --handler de.mbe.tutorials.aws.lambda.FnHelloHandlerWithPOJOs::handleRequest \
    --runtime java11 \
    --timeout 15 \
    --memory-size 256
```

### Update Function
Assuming the Gradle specific location of the fat jar:
```shell
aws lambda update-function-code \
    --function-name hello-aws-lambda-java11 \
    --zip-file fileb://build/libs/hello-aws-lambda-java11.jar
```

### Test Function
Run a simple test by invoking the function with a predefined input:
```shell
aws lambda invoke \
    --function-name hello-aws-lambda-java11 \
    --payload '{ "firstName": "Bogdan-Eugen", "lastName": "Mihai" }' \
    test_response.json && cat test_response.json && rm -f test_response.json
```

### Delete Function
Delete the Lambda function:
```shell
aws lambda delete-function --function-name hello-aws-lambda-java11
```

### Get Function
Return information about the function or function version, with a link to download the deployment package valid for 10 minutes:
```shell
aws lambda get-function --function-name hello-aws-lambda-java11
```

### List Functions
Return a list of Lambda functions, with the version-specific configuration of each:
```shell
aws lambda list-functions
```

### Other targets
Check the [Makefile](Makefile) for all available targets
```shell
make help
```

## Walkthrough

### Setup
When it comes to the build automation system, there are 2 major options - [Gradle](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#gradle) or [Maven](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#maven), both of them being perfectly identical from the end result perspective. Nevertheless, any other build automation system capable of outputting a fat-jar can be used.

By default, the Makefile is using the Maven setup.

#### Gradle
- Gradle [Java Plugin](https://docs.gradle.org/current/userguide/java_plugin.html) set up to work with JDK11:
```groovy
plugins {
    id "java"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
```
- Gradle [Shadow Plugin](https://github.com/johnrengelman/shadow) configured to package uber-jars:
```properties
shadowPluginVersion=5.2.0
```
```groovy
plugins {
    id "com.github.johnrengelman.shadow" version "${shadowPluginVersion}"
}

shadowDistZip.enabled = false
shadowDistTar.enabled = false
shadowJar.setArchiveVersion("")
shadowJar {
    archiveBaseName = "hello-aws-lambda-java11"
    archiveClassifier = ""
    archiveVersion = ""
}
```
- Dependency [com.amazonaws.aws-lambda-java-core](https://github.com/aws/aws-lambda-java-libs/tree/master/aws-lambda-java-core) - AWS Lambda Java support library:
```properties
awsLambdaJavaCoreDependencyVersion=1.2.0
```
```groovy
dependencies {
    implementation "com.amazonaws:aws-lambda-java-core:${awsLambdaJavaCoreDependencyVersion}"
}
```
- Dependency [io.symphonia.lambda-logging](https://github.com/symphoniacloud/lambda-monitoring/tree/master/lambda-logging) - Better logging for AWS Lambda Java using SLF4J and logback:
```properties
lambdaLoggingDependencyVersion=1.0.3
```
```groovy
dependencies {
    implementation "io.symphonia:lambda-logging:${lambdaLoggingDependencyVersion}:no-config"
}  
```
- Dependency [com.fasterxml.jackson.core.jackson-databind](https://github.com/FasterXML/jackson-databind) - Used only in the handler scenario using Streams:
```properties
jacksonDependencyVersion=2.10.1
```
```groovy
dependencies {
    implementation "com.fasterxml.jackson.core:jackson-databind:${jacksonDependencyVersion}"
}  
```
- Test dependency [org.junit.jupiter.junit-jupiter](https://github.com/junit-team/junit5/tree/master/junit-jupiter-api):
```properties
junitJupiterDependencyVersion=5.5.2
```
```groovy
dependencies {
    testImplementation "org.junit.jupiter:junit-jupiter:${junitJupiterDependencyVersion}"
}  

test {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
    }
}
```

#### Maven
- [maven-compiler-plugin](https://maven.apache.org/plugins/maven-compiler-plugin/) set up to work with JDK11
```xml
<properties>
    <maven-compiler-plugin.version>3.8.1</maven-compiler-plugin.version>
    <maven.compiler.release>11</maven.compiler.release>
</properties>
```
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${maven-compiler-plugin.version}</version>
    <configuration>
        <release>${maven.compiler.release}</release>
    </configuration>
</plugin>
```
- [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/) configured to package uber-jars
```xml
<packaging>jar</packaging>
```
```xml
<properties>
    <maven-shade-plugin.version>3.2.1</maven-shade-plugin.version>
</properties>
```
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>${maven-shade-plugin.version}</version>
    <configuration>
        <createDependencyReducedPom>false</createDependencyReducedPom>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
- Dependency [com.amazonaws.aws-lambda-java-core](https://github.com/aws/aws-lambda-java-libs/tree/master/aws-lambda-java-core) - AWS Lambda Java support library:
```xml
<aws-lambda-java-core.version>1.2.0</aws-lambda-java-core.version>
```
```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-core</artifactId>
    <version>${aws-lambda-java-core.version}</version>
</dependency>
```
- Dependency [io.symphonia.lambda-logging](https://github.com/symphoniacloud/lambda-monitoring/tree/master/lambda-logging) - Better logging for AWS Lambda Java using SLF4J and logback:
```xml
<lambda-logging.version>1.0.3</lambda-logging.version>
```
```xml
<dependency>
    <groupId>io.symphonia</groupId>
    <artifactId>lambda-logging</artifactId>
    <version>${lambda-logging.version}</version>
    <classifier>no-config</classifier>
</dependency>
```
- Dependency [com.fasterxml.jackson.core.jackson-databind](https://github.com/FasterXML/jackson-databind) - Used only in the handler scenario using Streams:
```xml
<jackson.version>2.10.1</jackson.version>
```
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>${jackson.version}</version>
</dependency>
```
- Test dependency [org.junit.jupiter.junit-jupiter](https://github.com/junit-team/junit5/tree/master/junit-jupiter-api):
```xml
<junit-jupiter.version>5.5.2</junit-jupiter.version>
```
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>${junit-jupiter.version}</version>
    <scope>test</scope>
</dependency>
```
### Handler
