const axios = require("axios")

const pino = require("pino")
const LOGGER = pino({ level: process.env.LOG_LEVEL || "info" })

const AWSXRay = require("aws-xray-sdk-core")
AWSXRay.captureHTTPsGlobal(require("http"));

const STATIC_RANDOM = Math.random()

exports.apiAsyncLambdaHandler = async (event, context) => {
    request = JSON.parse(event["body"])
    return {
        "statusCode": 200,
        "body": JSON.stringify(await internalAsyncLambdaHandler(request, context))
    }
}

exports.asyncLambdaHandler = internalAsyncLambdaHandler

async function internalAsyncLambdaHandler(event, context) {
    LOGGER.info(getRuntimeInfo())
    LOGGER.info(await getPublicIP())

    LOGGER.info(`${process.env.AWS_EXECUTION_ENV} called ${context.functionName} has ${context.getRemainingTimeInMillis() / 1000} seconds to live`)

    LOGGER.info(`Static value: ${STATIC_RANDOM}, invocation value: ${Math.random()}`)

    return processRequest(event, process.env.PACKAGE_MANAGEMENT_SYSTEM, "async")
}

exports.apiSyncLambdaHandler = function(event, context, callback) {
    request = JSON.parse(event["body"])

    LOGGER.info(getRuntimeInfo())

    LOGGER.info(`${process.env.AWS_EXECUTION_ENV} called ${context.functionName} has ${context.getRemainingTimeInMillis() / 1000} seconds to live`)

    LOGGER.info(`Static value: ${STATIC_RANDOM}, invocation value: ${Math.random()}`)

    callback(null, {
        "statusCode": 200,
        "body": JSON.stringify(processRequest(request, process.env.PACKAGE_MANAGEMENT_SYSTEM, "non-async"))
    })
}

exports.syncLambdaHandler = function(event, context, callback) {
    LOGGER.info(getRuntimeInfo())

    LOGGER.info(`${process.env.AWS_EXECUTION_ENV} called ${context.functionName} has ${context.getRemainingTimeInMillis() / 1000} seconds to live`)

    LOGGER.info(`Static value: ${STATIC_RANDOM}, invocation value: ${Math.random()}`)

    callback(null, processRequest(event, process.env.PACKAGE_MANAGEMENT_SYSTEM, "non-async"))
}

exports.exportedProcessRequest = (event, packageManagementSystem) => {
    return processRequest(event, packageManagementSystem)
}

function processRequest(event, packageManagementSystem, type) {
    if (event["throwError"]) {
        throw new Error("Sorry, but the caller wants to me to throw an error");
    }

    const greeting = `Hello ${event["firstName"]} ${event["lastName"]}, this is a Nodejs12.x AWS Lambda ${type} function developed using ${packageManagementSystem}!`

    const result = `Your age in hexadecimal is ${parseInt(event["age"], 10).toString(16)}`;

    return {
        "greeting": greeting,
        "result": result
    }
}

function getRuntimeInfo() {
    return AWSXRay.captureFunc("getRuntimeInfo", function(){
        return `node.js ${process.versions.node} v8 ${process.versions.v8}`
    });
}

async function getPublicIP() {
    try {
        const response = await axios("http://checkip.amazonaws.com/");
        if (response.status / 100 == 2) {
            return `Public IP:  ${response.data.trim()}`
        } else {
            return `Unknown public IP, status code ${response.status} received`
        }
    } catch (err) {
        LOGGER.error(err);
        return `Error: ${err.toString()}`
    }
}
