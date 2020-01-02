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
## create-lambda: Create the Lambda function using the default POJOs aproach - must pass along `AWS_ACCOUNT_ID` and `AWS_ROLE`
create-lambda: check-aws-cli check-aws-account-id check-aws-role package
ifeq ($(BUILD_AUTOMATION_SYSTEM), Maven)
	aws lambda create-function \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://target/${FUNCTION_NAME}.jar \
		--environment Variables={BUILD_AUTOMATION_SYSTEM=$(BUILD_AUTOMATION_SYSTEM)} \
		--role arn:aws:iam::$(AWS_ACCOUNT_ID):role/$(AWS_ROLE)  \
		--handler de.mbe.tutorials.aws.lambda.FnHelloHandlerWithPOJOs::handleRequest \
		--runtime java11 \
		--timeout 15 \
		--memory-size 256
else
	aws lambda create-function \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://build/libs/${FUNCTION_NAME}.jar \
		--environment Variables={BUILD_AUTOMATION_SYSTEM=$(BUILD_AUTOMATION_SYSTEM)} \
		--role arn:aws:iam::$(AWS_ACCOUNT_ID):role/$(AWS_ROLE)  \
		--handler de.mbe.tutorials.aws.lambda.FnHelloHandlerWithPOJOs::handleRequest \
		--runtime java11 \
		--timeout 15 \
		--memory-size 256
endif

.PHONY: create-lambda-streams
## create-lambda-streams: Create the Lambda function using the Streams aproach - must pass along `AWS_ACCOUNT_ID` and `AWS_ROLE`
create-lambda-streams: check-aws-cli check-aws-account-id check-aws-role package
ifeq ($(BUILD_AUTOMATION_SYSTEM), Maven)
	aws lambda create-function \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://target/${FUNCTION_NAME}.jar \
		--environment Variables={BUILD_AUTOMATION_SYSTEM=$(BUILD_AUTOMATION_SYSTEM)} \
		--role arn:aws:iam::$(AWS_ACCOUNT_ID):role/$(AWS_ROLE)  \
		--handler de.mbe.tutorials.aws.lambda.FnHelloHandlerWithStreams::handleRequest \
		--runtime java11 \
		--timeout 15 \
		--memory-size 256
else
	aws lambda create-function \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://build/libs/${FUNCTION_NAME}.jar \
		--environment Variables={BUILD_AUTOMATION_SYSTEM=$(BUILD_AUTOMATION_SYSTEM)} \
		--role arn:aws:iam::$(AWS_ACCOUNT_ID):role/$(AWS_ROLE)  \
		--handler de.mbe.tutorials.aws.lambda.FnHelloHandlerWithStreams::handleRequest \
		--runtime java11 \
		--timeout 15 \
		--memory-size 256
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
## test-lambda: Run a simple test by invoking the function wiht a predefined input
test-lambda: check-aws-cli
	aws lambda invoke \
		--function-name ${FUNCTION_NAME} \
    	--payload '{ "firstName": "Bogdan-Eugen", "lastName": "Mihai", "age": 34 }' \
    test_response.json && cat test_response.json && rm -f test_response.json

.PHONY: delete-lambda
## delete-lambda: Deletes the Lambda function
delete-lambda: check-aws-cli
	aws lambda delete-function --function-name ${FUNCTION_NAME}

.PHONY: get-lambda
## get-lambda: Returns information about the function or function version, with a link to download the deployment package valid for 10 mins
get-lambda: check-aws-cli
	aws lambda get-function --function-name ${FUNCTION_NAME}

.PHONY: list-lambda
## list-lambda: Returns a list of Lambda functions, with the version-specific configuration of each
list-lambda: check-aws-cli
	aws lambda list-functions

.PHONY: check-aws-cli
## check-aws-cli: Locate the AWS CLI in the current user's path (checking if it is installed or not)
check-aws-cli:
ifeq (, $(shell which aws))
	$(error "AWS CLI is NOT installed correctly. More information: https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html")
endif

check-aws-account-id:
## check-aws-account-id: Ensure the AWS_ACCOUNT_ID environment variable is defined
ifndef AWS_ACCOUNT_ID
	$(error "AWS_ACCOUNT_ID is undefined")
endif

check-aws-role:
## check-aws-role: Ensure the AWS_ROLE environment variable is defined
ifndef AWS_ROLE
	$(error "AWS_ROLE is undefined")
endif

.PHONY: help
## help: Prints this help message
help:
	@echo "Usage: \n"
	@sed -n 's/^##//p' ${MAKEFILE_LIST} | column -t -s ':' |  sed -e 's/^/ /'