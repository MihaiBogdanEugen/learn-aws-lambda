# AWS Lambda using Java with Java11 Runtime

This is an over-engineered "hello world" style AWS Lambda created using Java11 showing:
- deployment package prepared using either Gradle or Maven
- handler function using either POJOs or Streams
- execution context
- function context
- usage of reserved environment variables
- usage of custom environment variables
- separation of worker function for unit testing
- logging using SLF4J and logback
- error handling
- tracing using AWS X-Ray
- useful AWS CLI commands for creating, updating, invoking, getting, listing and deleting AWS Lambda resources

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
./mvnw clean && ./mvnw package
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
Check the [Makefiles](Makefile) for all avalable targets
```shell
make help
```

## Walkthrough
