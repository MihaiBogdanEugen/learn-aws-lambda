FUNCTION_NAME?=hello-aws-lambda-python3_8

.PHONY: clean
## clean: Clean the project-local libraries folder
clean:
	rm -rf temp/
	rm -rf package/

.PHONY: test
## test: Run the tests
test:
	python -m unittest tests/fn_hello_tests.py

.PHONY: package
## package: Install libraries in a new, project-local package directory with pip's --target option
package: clean test
	pip install -t ./temp -r requirements.txt && \
	cp -r app temp/ && \
	cd temp && \
	zip -r9 ${FUNCTION_NAME}.zip . && \
	cd .. &&\
	mkdir package &&\
	mv temp/${FUNCTION_NAME}.zip package/ && \
	rm -rf temp/

.PHONY: check-updates
## check-updates: Check for dependencies updates
check-updates:
	pip list --outdated

.PHONY: create-lambda
## create-lambda: Create the Lambda function using the default POJOs aproach - must pass along `AWS_ACCOUNT_ID` and `AWS_ROLE`
create-lambda: check-aws-cli check-aws-role-arn package
	aws lambda create-function \
		--function-name ${FUNCTION_NAME} \
    	--zip-file fileb://package/${FUNCTION_NAME}.zip \
		--environment Variables={PACKAGE_MANAGEMENT_SYSTEM=pip} \
		--role $(AWS_ROLE_ARN)  \
		--handler app/fn_hello.lambda_handler \
		--runtime python3.8 \
		--timeout 30 \
		--memory-size 256 \
		--tracing-config Mode=Active

.PHONY: update-lambda
## update-lambda: Update the Lambda's function code
update-lambda: check-aws-cli package
	aws lambda update-function-code \
		--function-name ${FUNCTION_NAME} \
		--zip-file fileb://package/${FUNCTION_NAME}.zip

.PHONY: test-lambda
## test-lambda: Run a simple test by invoking the function with a predefined input
test-lambda: check-aws-cli
	aws lambda invoke \
		--function-name ${FUNCTION_NAME} \
    	--payload '{ "first_name": "Bogdan-Eugen", "last_name": "Mihai", "age": 34, "throw_error": false }' \
    test_response.json && cat test_response.json && rm -f test_response.json

.PHONY: test-lambda-error
## test-lambda-error: Run a simple test by invoking the function with a predefined input
test-lambda-error: check-aws-cli
	aws lambda invoke \
		--function-name ${FUNCTION_NAME} \
    	--payload '{ "first_name": "Bogdan-Eugen", "last_name": "Mihai", "age": 34, "throw_error": true }' \
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