import logging
import platform
import os
import random
import requests

from aws_xray_sdk.core import xray_recorder
from aws_xray_sdk.core import patch_all

patch_all()

LOGGER = logging.getLogger()
LOGGER.setLevel(logging.INFO)

STATIC_RANDOM = random.random()


class SimpleException(Exception):
    pass


def lambda_handler(event, context):
    LOGGER.info(get_runtime_info())
    LOGGER.info(get_public_ip())

    LOGGER.info("{} called {} has {} seconds to live".format(
        os.environ["AWS_EXECUTION_ENV"], context.function_name, context.get_remaining_time_in_millis() / 1000))

    LOGGER.info("Static value : {}, invocation value: {}".format(STATIC_RANDOM, random.random()))

    return process_request(event, os.environ["PACKAGE_MANAGEMENT_SYSTEM"])


def process_request(event, package_management_system):
    if event["throw_error"]:
        raise SimpleException("Sorry, but the caller wants to me to throw an error")

    greeting = "Hello {} {}, this is a Python3.8 AWS Lambda function developed using {}!".format(
        event["first_name"], event["last_name"], package_management_system)

    result = "Your age in hexadecimal is {}".format(hex(int(event["age"])))

    return {
        "greeting": greeting,
        "result": result
    }


@xray_recorder.capture("getRuntimeInfo")
def get_runtime_info():
    return "{} {} {}".format(platform.python_implementation(), platform.python_version(), platform.python_compiler())


def get_public_ip():
    try:
        response = requests.get("http://checkip.amazonaws.com/")
        if response.status_code / 100 == 2:
            return "Public IP: {}".format(response.text.strip())
        else:
            return "Unknown public IP, status code {} received".format(response.status_code)
    except requests.RequestException as e:
        LOGGER.error(e)
        return "RequestException: {}".format(str(e))
