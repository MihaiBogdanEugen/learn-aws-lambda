FUNCTION_NAME?=hello-aws-lambda-java11
BUILD_AUTOMATION_SYSTEM?=Maven

.PHONY: clean
## clean: Clean the files and directories generated during build
clean:
ifeq ($(BUILD_AUTOMATION_SYSTEM), Maven)
	@(echo "Using Maven")
	./mvnw clean
else
	@(echo "Using Gradle")
	./gradlew clean
endif

.PHONY: test
## test: Run the tests
test: clean
ifeq ($(BUILD_AUTOMATION_SYSTEM), Maven)
	./mvnw test
else
	./gradlew test
endif

.PHONY: package
## package: Build and package the source code into an uber-jar
package: test
ifeq ($(BUILD_AUTOMATION_SYSTEM), Maven)
	./mvnw package
else
	./gradlew shadowJar
endif

.PHONY: check-updates
## check-updates: Check for dependencies updates
check-updates:
ifeq ($(BUILD_AUTOMATION_SYSTEM), Maven)
	@(echo "Using Maven")
	./mvnw versions:display-plugin-updates
else
	@(echo "Using Gradle")
	./gradlew dependencyUpdates
endif

.PHONY: create-lambda
## create-lambda: Create the Lambda function using the default POJOs aproach - must pass along `AWS_ROLE_ARN`
create-lambda: check-aws-cli check-aws-role-arn package
ifeq ($(BUILD_AUTOMATION_SYSTEM), Maven)
	aws lambda create-function \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://target/${FUNCTION_NAME}.jar \
		--environment Variables={BUILD_AUTOMATION_SYSTEM=$(BUILD_AUTOMATION_SYSTEM)} \
		--role $(AWS_ROLE_ARN)  \
		--handler de.mbe.tutorials.aws.lambda.FnHelloHandlerWithPOJOs::handleRequest \
		--runtime java11 \
		--timeout 30 \
		--memory-size 256 \
		--tracing-config Mode=Active
else
	aws lambda create-function \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://build/libs/${FUNCTION_NAME}.jar \
		--environment Variables={BUILD_AUTOMATION_SYSTEM=$(BUILD_AUTOMATION_SYSTEM)} \
		--role $(AWS_ROLE_ARN)  \
		--handler de.mbe.tutorials.aws.lambda.FnHelloHandlerWithPOJOs::handleRequest \
		--runtime java11 \
		--timeout 30 \
		--memory-size 256 \
		--tracing-config Mode=Active
endif

.PHONY: create-lambda-streams
## create-lambda-streams: Create the Lambda function using the Streams aproach - must pass along `AWS_ROLE_ARN`
create-lambda-streams: check-aws-cli check-aws-role-arn package
ifeq ($(BUILD_AUTOMATION_SYSTEM), Maven)
	aws lambda create-function \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://target/${FUNCTION_NAME}.jar \
		--environment Variables={BUILD_AUTOMATION_SYSTEM=$(BUILD_AUTOMATION_SYSTEM)} \
		--role $(AWS_ROLE_ARN)  \
		--handler de.mbe.tutorials.aws.lambda.FnHelloHandlerWithStreams::handleRequest \
		--runtime java11 \
		--timeout 30 \
		--memory-size 256 \
		--tracing-config Mode=Active
else
	aws lambda create-function \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://build/libs/${FUNCTION_NAME}.jar \
		--environment Variables={BUILD_AUTOMATION_SYSTEM=$(BUILD_AUTOMATION_SYSTEM)} \
		--role $(AWS_ROLE_ARN)  \
		--handler de.mbe.tutorials.aws.lambda.FnHelloHandlerWithStreams::handleRequest \
		--runtime java11 \
		--timeout 30 \
		--memory-size 256 \
		--tracing-config Mode=Active
endif

.PHONY: update-lambda
## update-lambda: Update the Lambda's function code
update-lambda: check-aws-cli package
ifeq ($(BUILD_AUTOMATION_SYSTEM), Maven)
	aws lambda update-function-code \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://target/${FUNCTION_NAME}.jar
else
	aws lambda update-function-code \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://build/libs/${FUNCTION_NAME}.jar
endif

.PHONY: test-lambda
## test-lambda: Run a simple test by invoking the function with a predefined input
test-lambda: check-aws-cli
	aws lambda invoke \
		--function-name ${FUNCTION_NAME} \
    	--payload '{ "firstName": "Bogdan-Eugen", "lastName": "Mihai", "age": 34 }' \
    test_response.json && cat test_response.json && rm -f test_response.json

.PHONY: test-lambda-error
## test-lambda-error: Run a simple test by invoking the function with a predefined input
test-lambda-error: check-aws-cli
	aws lambda invoke \
		--function-name ${FUNCTION_NAME} \
    	--payload '{ "firstName": "Bogdan-Eugen", "lastName": "Mihai", "age": 34, "throwError": true }' \
    test_response.json && cat test_response.json && rm -f test_response.json

.PHONY: delete-lambda
## delete-lambda: Delete the Lambda function
delete-lambda: check-aws-cli
	aws lambda delete-function --function-name ${FUNCTION_NAME}

.PHONY: get-lambda
## get-lambda: Return information about the function or function version, with a link to download the deployment package valid for 10 minutes
get-lambda: check-aws-cli
	aws lambda get-function --function-name ${FUNCTION_NAME}

.PHONY: list-lambda
## list-lambda: Return a list of Lambda functions, with the version-specific configuration of each
list-lambda: check-aws-cli
	aws lambda list-functions

.PHONY: check-aws-cli
## check-aws-cli: Locate the AWS CLI in the current user's path (checking if it is installed or not)
check-aws-cli:
ifeq (, $(shell which aws))
	$(error "AWS CLI is NOT installed correctly. More information: https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html")
endif

check-aws-role-arn:
## check-aws-role-arn: Ensure the AWS_ROLE_ARN environment variable is defined
ifndef AWS_ROLE_ARN
	$(error "AWS_ROLE_ARN is undefined")
endif

.PHONY: help
## help: Prints this help message
help:
	@echo "Usage: \n"
	@sed -n 's/^##//p' ${MAKEFILE_LIST} | column -t -s ':' |  sed -e 's/^/ /'