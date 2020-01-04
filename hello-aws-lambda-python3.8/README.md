# AWS Lambda using Python with Python3.8 Runtime

This is an *over-engineered* "hello world" style AWS Lambda created using Python3.8 showing: 
- [deployment package](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#setup) prepared installing the dependencies into a dedicated folder and zipping it together with the sources
- [handler function](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#handler) 
- [execution context](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#execution-context)
- [function context](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#function-context)
- [usage of reserved environment variables](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#reserved-environment-variables)
- [usage of custom environment variables](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#custom-environment-variables)
- [separation of worker function for unit testing](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#code-separation)
- [logging](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#logging)
- [error handling](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#error-handling)
- [tracing using AWS X-Ray](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#tracing)
- useful AWS CLI commands for
  - [creating](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#create-function) AWS Lambda resources
  - [updating](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#update-function) AWS Lambda resources
  - [invoking](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#test-function) AWS Lambda resources
  - [getting](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#get-function) AWS Lambda resources
  - [listing](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#list-function) AWS Lambda resources
  - [deleting](https://github.com/MihaiBogdanEugen/learn-aws-lambda/tree/master/hello-aws-lambda-python3.8#delete-function) AWS Lambda resources
## Requirements
- [Python 3.8.x](https://www.python.org/downloads/)
- [AWS Command Line Interface](https://aws.amazon.com/cli/)
- AWS_ROLE_ARN - AWS resource name of the role used for the Lambda function. At a bare minimum, the role must be equivalent with attaching the following AWS managed policies:
  - AWSLambdaBasicExecutionRole - provides limited write access to AWS CloudWatch Logs 
  - AWSXrayWriteOnlyAccess - provides limited read write access to AWS X-Ray
    
## Start

### Test and Package the Function Code
```shell
rm -rf package/ && \
python -m unittest tests/fn_hello_tests.py && \
pip install -t ./package -r requirements.txt && \
cp -r app package/ && \
cd package && \
zip -r9 hello-aws-lambda-python3_8.zip .
```
The output package is a fat-zip located in the package folder:
```shell script
package/hello-aws-lambda-python3_8.zip
```

### Create Function
Assuming the default location of the fat-zip:
```shell
aws lambda create-function \
    --function-name hello-aws-lambda-python3_8 \
    --zip-file fileb://package/hello-aws-lambda-python3_8.zip \
    --environment Variables={PACKAGE_MANAGEMENT_SYSTEM=pip} \
    --role $(AWS_ROLE_ARN)  \
    --handler app/fn_hello.lambda_handler \
    --runtime python3.8 \
    --timeout 30 \
    --memory-size 256 \
    --tracing-config Mode=Active
```

### Update Function
Assuming the default location of the fat-zip:
```shell
aws lambda update-function-code \
		--function-name hello-aws-lambda-python3_8 \
		--zip-file fileb://package/hello-aws-lambda-python3_8.zip
```

### Test Function
Run a simple test by invoking the function with a predefined input:
```shell
aws lambda invoke \
    --function-name hello-aws-lambda-python3_8 \
    --payload '{ "firstName": "Bogdan-Eugen", "lastName": "Mihai", "age": 34, "throwError": false }' \
    test_response.json && cat test_response.json && rm -f test_response.json
```

### Delete Function
Delete the Lambda function:
```shell
aws lambda delete-function --function-name hello-aws-lambda-python3_8
```

### Get Function
Return information about the function or function version, with a link to download the deployment package valid for 10 minutes:
```shell
aws lambda get-function --function-name hello-aws-lambda-python3_8
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
The setup is based on using pip with a requirements file containing the dependencies:
```properties
aws-xray-sdk==2.4.3
requests==2.22.0
```
The main code of the Lambda function is located in the `app` package:
```shell
app/fn_hello.py
```

### Handler
The handler function is as simple as it can be:
```python
def lambda_handler(event, context):
    pass
```
The first argument (`event`) represent a dictionary based on the JSON-ified invocation input, while the second argument (`context`) represents the actual function context.

### Execution Context
If one declares a variable outside the handler function, its value will not change during subsequent invocations, contrary to variables defined inside the handler functions, which will change every time the function is invoked:
```python
import logging
import random

LOGGER = logging.getLogger()
LOGGER.setLevel(logging.INFO)

STATIC_RANDOM = random.random()

def lambda_handler(event, context):
    LOGGER.info("Static value : {}, invocation value: {}".format(STATIC_RANDOM, random.random()))
```

### Function Context
The [context](https://docs.aws.amazon.com/lambda/latest/dg/python-context-object.html) object provides access to the function's invocation context:
```python
import logging
import os

LOGGER = logging.getLogger()
LOGGER.setLevel(logging.INFO)

def lambda_handler(event, context):
    LOGGER.info("{} called {} has {} seconds to live".format(
        os.environ["AWS_EXECUTION_ENV"], context.function_name, context.get_remaining_time_in_millis() / 1000))
```

### Reserved Environment Variables
There are [reserved environment variables](https://docs.aws.amazon.com/lambda/latest/dg/lambda-environment-variables.html) available to Lambda functions. 
The previous code sample shows the usage of `AWS_EXECUTION_ENV` value.

### Custom Environment Variables
Custom environment variables can be used if they are passed along when the AWS Lambda function is created:
```shell
aws lambda create-function \
    (...)       
    --environment Variables={PACKAGE_MANAGEMENT_SYSTEM=pip} \
    (...)
```

### Code Separation
No matter the preference regarding the interface to implement, it is always a wise idea to separate the actual business logic code by the AWS specific handler function.
```python
def lambda_handler(event, context):
    process_request(event, "")

def process_request(event, package_management_system):
    pass
```

### Logging
The standard Python logging library can be used for all logging purposes:
```python
import logging

LOGGER = logging.getLogger()
LOGGER.setLevel(logging.INFO)

def lambda_handler(event, context):
    LOGGER.info("...")
```

### Error Handling
If your Lambda function code throws an exception, the AWS Lambda runtime recognizes the failure and serializes the exception information into JSON and returns it:
```json
{
  "errorMessage": "Sorry, but the caller wants to me to throw an error", 
  "errorType": "SimpleException", 
  "stackTrace": [
    "  File \"/var/task/app/fn_hello.py\", line 31, in lambda_handler\n    return process_request(event, os.environ[\"PACKAGE_MANAGEMENT_SYSTEM\"])\n", 
    "  File \"/var/task/app/fn_hello.py\", line 36, in process_request\n    raise SimpleException(\"Sorry, but the caller wants to me to throw an error\")\n"
  ]
}
```

### Tracing
AWS Lambda functions can easily use X-Ray for tracing by adding the [aws-xray-sdk ](https://github.com/aws/aws-xray-sdk-python) library.

- For a more complex behaviour, one can use custom subsegments. Here is an example of adding such a subsegment using the decorator annotation:
```python
import platform

from aws_xray_sdk.core import xray_recorder

@xray_recorder.capture("getRuntimeInfo")
def get_runtime_info():
    return "{} {} {}".format(platform.python_implementation(), platform.python_version(), platform.python_compiler())
```
- To instrument downstream calls, use the X-Ray SDK for Python to patch the libraries that your application uses. The X-Ray SDK for Python can patch the `request` library as follows:

```python
import requests

from aws_xray_sdk.core import xray_recorder
from aws_xray_sdk.core import patch_all

patch_all()
``` 

Don't forget, in AWS Lambda, you cannot modify the sampling rate. If your function is called by an instrumented service, calls that generated requests that were sampled by that service will be recorded by Lambda. If active tracing is enabled and no tracing header is present, Lambda makes the sampling decision.