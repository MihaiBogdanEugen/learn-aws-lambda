FUNCTION_NAME?=hello-aws-lambda-python3dot8

.PHONY: clean
## clean: Clean the project-local libraries folder
clean:
	rm -rf temp/ &&\
	rm -rf package/
#	rm -rf venv/

.PHONY: install
## install: Installs dependencies from requirements.txt
install:
	pip install -r requirements.txt

.PHONY: test
## test: Run the tests
test: install
	python -m unittest tests/fn_hello_tests.py

.PHONY: package
## package: Install libraries in a new, local temp directory with pip, and zip everything into a fat-zip
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
## create-lambda: Create the Lambda function - must pass along `AWS_ROLE_ARN`
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

.PHONY: upload-package-to-s3
## upload-package-to-s3: Upload the package to a predefined S3 bucket
upload-package-to-s3: package check-aws-cli check-lambda-src-bucket
	aws s3 cp package/${FUNCTION_NAME}.zip s3://${LAMBDA_SRC_BUCKET}/${FUNCTION_NAME}.zip

.PHONY: validate-deployment
## validate-deployment: Validate the deployment AWS SAM template
validate-deployment: check-aws-sam
	sam validate --template-file sam.yaml

.PHONY: deploy
## deploy: Create a Cloudformation Stack and deploys your resources
deploy: upload-package-to-s3 check-aws-sam check-lambda-src-bucket
	sam deploy \
		--region $(shell aws configure get region) \
		--template-file sam.yaml \
		--stack-name ${FUNCTION_NAME} \
		--capabilities CAPABILITY_IAM

.PHONY: test-live
## test-live: Do a test against the PROD version of the API Gateway and Lambda function
test-live: check-jq
	curl --location --request POST $(shell aws cloudformation describe-stacks --stack-name hello-aws-lambda-python3dot8 | jq '.Stacks[0].Outputs[2].OutputValue') \
		--header 'Content-Type: application/json' \
		--data-raw '{ "first_name": "Bogdan-Eugen", "last_name": "Mihai", "age": 34, "throw_error": false }'

.PHONY: delete-stack
## delete-stack: Deletes the specified stack
delete-stack: check-aws-cli
	aws cloudformation delete-stack --stack-name ${FUNCTION_NAME}

.PHONY: check-jq
## check-jq: Locate jq in the current user's path (checking if it is installed or not)
check-jq:
ifeq (, $(shell which jq))
	$(error "jq is NOT installed correctly")
endif

.PHONY: check-aws-cli
## check-aws-cli: Locate the AWS CLI in the current user's path (checking if it is installed or not)
check-aws-cli:
ifeq (, $(shell which aws))
	$(error "AWS CLI is NOT installed correctly. More information: https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html")
endif

.PHONY: check-aws-sam
## check-aws-sam: Locate the AWS SAM in the current user's path (checking if it is installed or not)
check-aws-sam:
ifeq (, $(shell which aws))
	$(error "AWS SAM is NOT installed correctly. More information: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html")
endif

.PHONY: check-aws-role-arn
## check-aws-role-arn: Ensure the AWS_ROLE_ARN environment variable is defined
check-aws-role-arn:
ifndef AWS_ROLE_ARN
	$(error "AWS_ROLE_ARN is undefined")
endif

.PHONY: check-lambda-src-bucket
## check-lambda-src-bucket: Ensure the LAMBDA_SRC_BUCKET environment variable is defined
check-lambda-src-bucket:
ifndef LAMBDA_SRC_BUCKET
	$(error "LAMBDA_SRC_BUCKET is undefined")
endif

.PHONY: help
## help: Prints this help message
help:
	@echo "Usage: \n"
	@sed -n 's/^##//p' ${MAKEFILE_LIST} | column -t -s ':' |  sed -e 's/^/ /'