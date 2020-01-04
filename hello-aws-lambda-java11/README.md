# AWS Lambda using Java with Java11 Runtime

This is an *over-engineered* "hello world" style AWS Lambda created using Java11 showing:
- [deployment package](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#setup) prepared using either [Gradle](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#gradle) or [Maven](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#maven)
- [handler function](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#handler) using either [POJOs](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#pojo-approach-based-on-implementing-the-requesthandler-interface) or [Streams](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#streams-approach-based-on-implementing-the-requeststreamhandler-interface)
- [execution context](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#execution-context)
- [function context](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#function-context)
- [usage of reserved environment variables](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#reserved-environment-variables)
- [usage of custom environment variables](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#custom-environment-variables)
- [separation of worker function for unit testing](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#code-separation)
- [logging](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#logging) using SLF4J and logback
- [error handling](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#error-handling)
- [tracing using AWS X-Ray](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#tracing)
- useful AWS CLI commands for
  - [creating](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#create-function) AWS Lambda resources
  - [updating](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#update-function) AWS Lambda resources
  - [invoking](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#test-function) AWS Lambda resources
  - [getting](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#get-function) AWS Lambda resources
  - [listing](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#list-function) AWS Lambda resources
  - [deleting](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#delete-function) AWS Lambda resources

## Requirements
- [Amazon Corretto JDK 11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
- [AWS Command Line Interface](https://aws.amazon.com/cli/)
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
    --timeout 30 \
    --memory-size 256 \
    --tracing-config Mode=Active
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
When it comes to the build automation system, there are 2 major options:
- [Gradle](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#gradle) or 
- [Maven](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-java11#maven).

Both of them being perfectly identical from the end result perspective. Nevertheless, any other build automation system capable of outputting a fat-jar can be used.

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
- Dependency [com.amazonaws.aws-xray-recorder-sdk-core](https://github.com/aws/aws-xray-sdk-java/tree/master/aws-xray-recorder-sdk-aws-sdk-core) - AWS X-Ray Java support library:
```properties
awsXrayRecorderSdkCoreDependencyVersion=2.4.0
```
```groovy
dependencies {
    implementation "com.amazonaws:aws-xray-recorder-sdk-core:${awsXrayRecorderSdkCoreDependencyVersion}"
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
- Dependency [com.amazonaws.aws-xray-recorder-sdk-core](https://github.com/aws/aws-xray-sdk-java/tree/master/aws-xray-recorder-sdk-aws-sdk-core) - AWS X-Ray Java support library:
```xml
<aws-xray-recorder-sdk-core.version>2.4.0</aws-xray-recorder-sdk-core.version>
```
```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-xray-recorder-sdk-core</artifactId>
    <version>${aws-xray-recorder-sdk-core.version}</version>
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
When it comes to the handler function, there are 2 interfaces one can implement:
- [RequestHandler](https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-core/src/main/java/com/amazonaws/services/lambda/runtime/RequestHandler.java) - This is the POJOs approach, which implies that the AWS Lambda runtime will `serialize/deserialize` the invocation input String to the predefined Request POJO and then the Response POJO to an output String 
- [RequestStreamHandler](https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-core/src/main/java/com/amazonaws/services/lambda/runtime/RequestStreamHandler.java) - This is the Streams approach, which implies no conversion at all, one has access to the raw InputStream and OutputStream. If the business logic can work natively with streams, this approach might be more effective, otherwise converting to POJOs and back might prove too costly.

By default, the Makefile is using the POJOs setup.

#### POJO approach based on implementing the [RequestHandler](https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-core/src/main/java/com/amazonaws/services/lambda/runtime/RequestHandler.java) interface:
```java
public class FnHelloHandlerWithPOJOs implements RequestHandler<Request, Response> {

    @Override
    public Response handleRequest(Request request, Context context) { }

}
```

#### Streams approach based on implementing the [RequestStreamHandler](https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-core/src/main/java/com/amazonaws/services/lambda/runtime/RequestStreamHandler.java) interface:
```java
public class FnHelloHandlerWithStreams implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

    }
}
```

### Execution Context
- The `STATIC_RANDOM` is a static field, its value will *NOT* change during subsequent invocations:
```java
public class FnHello implements RequestHandler<Request, Response> {
    private static final double STATIC_RANDOM = Math.random();
}
```
- The `constructorRandom` is initialized in the class' constructor, its value will *NOT* change during subsequent invocations:
```java
public class FnHello implements RequestHandler<FnHello.Request, FnHello.Response> {

    private final double constructorRandom;

    public FnHello() {
        this.constructorRandom = Math.random();
    }
}
```
- The `invocationRandom` is initialized during each invocation, its value will therefore always change:
```java
public class FnHello implements RequestHandler<Request, Response> {

    @Override
    public Response handleRequest(Request request, Context context) {
        final double invocationRandom = Math.random();
    }
}
```

### Function Context
The [context](https://docs.aws.amazon.com/lambda/latest/dg/java-context-object.html) object provides access to the function's invocation context:
```java
public class FnHello implements RequestHandler<Request, Response> {

    @Override
    public Response handleRequest(Request request, Context context) {

        LOGGER.info("{} called {} has {} seconds to live",
                System.getenv("AWS_EXECUTION_ENV"),
                context.getFunctionName(),
                context.getRemainingTimeInMillis() / 1000);
    }
}
```

### Reserved Environment Variables
There are [reserved environment variables](https://docs.aws.amazon.com/lambda/latest/dg/lambda-environment-variables.html) available to Lambda functions. 
The previous code sample shows the usage of `AWS_EXECUTION_ENV` value.

### Custom Environment Variables
Custom environment variables can be used if they are passed along when the AWS Lambda function is created:
```shell
aws lambda create-function \
    (...)       
    --environment Variables={BUILD_AUTOMATION_SYSTEM=Gradle} \
    (...)
```

### Code Separation
No matter the preference regarding the interface to implement, it is always a wise idea to separate the actual business logic code by the AWS specific handler function.
```java
import static de.mbe.tutorials.aws.lambda.Processor.processRequest;

public class FnHello implements RequestHandler<Request, Response> {

    @Override
    public Response handleRequest(Request request, Context context) {

        return processRequest(request);
    }
}
```

```java
public final class Processor {

    public static Response processRequest(Request request) {

        return new Response();
    }
}
```

Therefore, unit testing the actual code becomes much easier:
```java
import static de.mbe.tutorials.aws.lambda.Processor.processRequest;

public class ProcessorTests {
    
    @Test
    void testProcessRequestNoError() {

        final Response response = processRequest(request);
        assertNotNull(response);
    }
}
```

### Logging
Use SL4J/logback logging:
```java
public class FnHello implements RequestHandler<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FnHello.class);

    @Override
    public Response handleRequest(Request request, Context context) {
        LOGGER.info("..."); 
    }
}
```
If you plan to modify the default logback configuration, set the classifier to `no-config` to the dependency entry.

Afterwards, add the `logback.xml` file in `src/main/resources`:
```xml
<configuration>
    <appender name="STDOUT" class="io.symphonia.lambda.logging.DefaultConsoleAppender">
        <encoder>
            <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] %X{AWSRequestId:-" + NO_REQUEST_ID + "} %.-6level %logger{5} - %msg \r%replace(%ex){'\n','\r'}%nopex%n</pattern>
        </encoder>
    </appender>
    <root level="info">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

### Error Handling
Pay attention to the signature of the handler functions:
- the signature of the handler method of the [RequestHandler](https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-core/src/main/java/com/amazonaws/services/lambda/runtime/RequestHandler.java) interface does not allow any checked exceptions
- the signature of the handler method of the [RequestStreamHandler](https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-core/src/main/java/com/amazonaws/services/lambda/runtime/RequestStreamHandler.java) interface allows only IOExceptions

Apart from this, if your Lambda function code throws an exception, the AWS Lambda runtime recognizes the failure and serializes the exception information into JSON and returns it:  
```json
{
  "errorMessage":"Sorry, but the caller wants to me to throw an error",
  "errorType":"de.mbe.tutorials.aws.lambda.SimpleRuntimeException",
  "stackTrace":[
    "de.mbe.tutorials.aws.lambda.Processor.processRequest(Processor.java:28)",
    "de.mbe.tutorials.aws.lambda.FnHelloHandlerWithPOJOs.handleRequest(FnHelloHandlerWithPOJOs.java:36)",
    "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
    "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)",
    "java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)",
    "java.base/java.lang.reflect.Method.invoke(Unknown Source)"
  ]
}
```
### Tracing
AWS Lambda functions can easily use X-Ray for tracing by importing the [com.amazonaws.aws-xray-recorder-sdk-core](https://github.com/aws/aws-xray-sdk-java/tree/master/aws-xray-recorder-sdk-aws-sdk-core) dependency into the function code - no other changes needed.

For a more complex behaviour, one can use custom subsegments.

- here is how to use the Subsegment Lambda Function to create a simple subsegment called `getRuntimeInfo`
```java
public final class Processor {
    public static String getRuntimeInfo() {
    
        return AWSXRay.createSubsegment("getRuntimeInfo", () -> {
            final RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
            return String.format("%s %s %s", mxBean.getVmVendor(), mxBean.getVmName(), mxBean.getVmVersion());
        });
    }
}
``` 

- here is a more advanced subsegment usage example, showcasing how to add HTTP specific metadata:
```java
public final class Processor {
    public static String getPublicIP() {

        final Subsegment subsegment = AWSXRay.beginSubsegment("getPublicIP");
        subsegment.setNamespace(Namespace.REMOTE.toString());

        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://checkip.amazonaws.com/"))
                    .header(TraceHeader.HEADER_KEY, getTraceHeaderKey(subsegment))
                    .GET()
                    .build();

            subsegment.putHttp("request", getRequestInformation(request));

            final HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            subsegment.putHttp("response", getResponseInformation(response));

            return response.statusCode() / 100 == 2
                    ? String.format("Public IP: %s", response.body().strip())
                    : String.format("Unknown public IP, status code %d received", response.statusCode());

        } catch (IOException e) {
            subsegment.addException(e);
            return String.format("IOException: %s", e.getMessage());
        } catch (InterruptedException e) {
            subsegment.addException(e);
            return String.format("InterruptedException: %s", e.getMessage());
        } finally {
            subsegment.close();
        }
    }
}
```
The previous code sample makes use of the new HttpClient included in Java11. If one wants to use the Apache HttpClient included into [org.apache.httpcomponents.httpclient](https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient) library, then AWS X-Ray offers direct integration by using the [com.amazonaws.aws-xray-recorder-sdk-aws-sdk-core](https://github.com/aws/aws-xray-sdk-java/tree/master/aws-xray-recorder-sdk-apache-http) dependency.

Don't forget, in AWS Lambda, you cannot modify the sampling rate. If your function is called by an instrumented service, calls that generated requests that were sampled by that service will be recorded by Lambda. If active tracing is enabled and no tracing header is present, Lambda makes the sampling decision.