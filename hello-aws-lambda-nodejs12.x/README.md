# AWS Lambda using Javascript with Nodejs12.x Runtime

This is an *over-engineered* "hello world" style AWS Lambda created using Nodejs12.x showing: 
- [deployment package](#setup) prepared installing the dependencies into a dedicated folder and zipping it together with the sources
- [handler function](#handler) 
- [execution context](#execution-context)
- [function context](#function-context)
- [usage of reserved environment variables](#reserved-environment-variables)
- [usage of custom environment variables](#custom-environment-variables)
- [separation of worker function for unit testing](#code-separation)
- [logging](#logging)
- [error handling](#error-handling)
- [tracing using AWS X-Ray](#tracing)
- useful AWS CLI commands for
  - [creating](#create-function) AWS Lambda resources
  - [updating](#update-function) AWS Lambda resources
  - [invoking](#test-function) AWS Lambda resources
  - [getting](#get-function) AWS Lambda resources
  - [listing](#list-function) AWS Lambda resources
  - [deleting](#delete-function) AWS Lambda resources

## Requirements
- [Nodejs 12.x](https://nodejs.org/en/download/)
- [AWS Command Line Interface](https://aws.amazon.com/cli/)
- AWS_ROLE_ARN - AWS resource name of the role used for the Lambda function. At a bare minimum, the role must be equivalent with attaching the following AWS managed policies:
  - AWSLambdaBasicExecutionRole - provides limited write access to AWS CloudWatch Logs 
  - AWSXrayWriteOnlyAccess - provides limited read write access to AWS X-Ray
    
## Start

### Test and Package the Function Code
```shell
rm -rf temp/ &&\
rm -rf package/ &&\
rm -rf node_modules/ && \
mkdir temp && \
cp package.json temp/ && \
cp fnHello.js temp/ && \
cd temp && \
npm install && \
zip -r9 /hello-aws-lambda-nodejs12_x.zip . && \
cd .. &&\
mkdir package &&\
mv temp/hello-aws-lambda-nodejs12_x.zip package/ && \
rm -rf temp/
```
The output package is a fat-zip located in the package folder:
```shell script
package/hello-aws-lambda-nodejs12_x.zip
```

### Create Function
Assuming the default location of the fat-zip:
```shell
aws lambda create-function \
    --function-name hello-aws-lambda-nodejs12_x \
    --zip-file fileb://package/hello-aws-lambda-nodejs12_x.zip \
    --environment Variables={PACKAGE_MANAGEMENT_SYSTEM=npm} \
    --role $(AWS_ROLE_ARN)  \
    --handler fnHello.lambdaHandler \
    --runtime nodejs12.x \
    --timeout 30 \
    --memory-size 256 \
    --tracing-config Mode=Active
```

### Update Function
Assuming the default location of the fat-zip:
```shell
aws lambda update-function-code \
    --function-name hello-aws-lambda-nodejs12_x \
    --zip-file fileb://package/hello-aws-lambda-nodejs12_x.zip
```

### Test Function
Run a simple test by invoking the function with a predefined input:
```shell
aws lambda invoke \
    --function-name hello-aws-lambda-nodejs12_x \
    --payload '{ "firstName": "Bogdan-Eugen", "lastName": "Mihai", "age": 34, "throwError": false }' \
test_response.json && cat test_response.json && rm -f test_response.json
```

### Delete Function
Delete the Lambda function:
```shell
aws lambda delete-function --function-name hello-aws-lambda-nodejs12_x
```

### Get Function
Return information about the function or function version, with a link to download the deployment package valid for 10 minutes:
```shell
aws lambda get-function --function-name hello-aws-lambda-nodejs12_x
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
The setup is based on using npm with a `package.json` file containing the dependencies:
```json
{
    "dependencies": {
        "axios": "0.19.0",
        "pino": "5.15.0"
    },
    "devDependencies": {
        "chai": "4.2.0",
        "mocha": "7.0.0"
    },
}
```
The main code of the Lambda function is located in the base package:
```shell
fnHello.js
```

### Handler
The handler function is as simple as it can be:
```javascript
exports.lambdaHandler = async (event, context) => {
}
```
The first argument (`event`) represent a dictionary based on the JSON-ified invocation input, while the second argument (`context`) represents the actual function context.

### Execution Context
If one declares a variable outside the handler function, its value will not change during subsequent invocations, contrary to variables defined inside the handler functions, which will change every time the function is invoked:
```javascript
const pino = require("pino")

const LOGGER = pino({ level: process.env.LOG_LEVEL || "info" })

const STATIC_RANDOM = Math.random()

exports.lambdaHandler = async (event, context) => {
    LOGGER.info(`Static value: ${STATIC_RANDOM}, invocation value: ${Math.random()}`)
}
```

### Function Context
The [context](https://docs.aws.amazon.com/lambda/latest/dg/nodejs-prog-model-context.html) object provides access to the function's invocation context:
```javascript
exports.lambdaHandler = async (event, context) => {
    LOGGER.info(`${process.env.AWS_EXECUTION_ENV} called ${context.functionName} has ${context.getRemainingTimeInMillis() / 1000} seconds to live`)
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
    --environment Variables={PACKAGE_MANAGEMENT_SYSTEM=npm} \
    (...)
```

### Code Separation
No matter the preference regarding the interface to implement, it is always a wise idea to separate the actual business logic code by the AWS specific handler function.
```javascript
exports.lambdaHandler = async (event, context) => {
    return processRequest(event, process.env.PACKAGE_MANAGEMENT_SYSTEM)
}

function processRequest(event, packageManagementSystem) {
}
```

### Logging
Any Nodejs logging library can be used for any logging purposes:
```javascript
const pino = require("pino")

const LOGGER = pino({ level: process.env.LOG_LEVEL || "info" })

exports.lambdaHandler = async (event, context) => {
    LOGGER.info("...")
}
```

### Error Handling
If your Lambda function code throws an exception, the AWS Lambda runtime recognizes the failure and serializes the exception information into JSON and returns it:
```json
{
    "errorType":"Error",
    "errorMessage":"Sorry, but the caller wants to me to throw an error",
    "trace":[
        "Error: Sorry, but the caller wants to me to throw an error",
        "    at processRequest (/var/task/fnHello.js:25:15)",
        "    at Runtime.exports.lambdaHandler [as handler] (/var/task/fnHello.js:16:12)",
        "    at processTicksAndRejections (internal/process/task_queues.js:93:5)"
    ]
}
```

### Tracing
AWS Lambda functions can easily use X-Ray for tracing by adding the [aws-xray-sdk](https://github.com/aws/aws-xray-sdk-node) library.

-tbd

Don't forget, in AWS Lambda, you cannot modify the sampling rate. If your function is called by an instrumented service, calls that generated requests that were sampled by that service will be recorded by Lambda. If active tracing is enabled and no tracing header is present, Lambda makes the sampling decision.