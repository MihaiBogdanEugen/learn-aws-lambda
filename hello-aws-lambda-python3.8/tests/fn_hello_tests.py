import unittest

from app import fn_hello


class TestProcessRequest(unittest.TestCase):

    def test_process_request_no_error(self):

        event = {
            "first_name": "Bogdan-Eugen",
            "last_name": "Mihai",
            "age": 34,
            "throw_error": False
        }
        response = fn_hello.process_request(event, "pip")

        self.assertEqual("Hello Bogdan-Eugen Mihai, this is a Python3.8 AWS Lambda function developed using pip!",
                         response["greeting"])
        self.assertEqual("Your age in hexadecimal is 0x22", response["result"])

    def test_process_request_throw_error(self):

        event = {
            "first_name": "Bogdan-Eugen",
            "last_name": "Mihai",
            "age": 34,
            "throw_error": True
        }

        with self.assertRaises(Exception):
            fn_hello.process_request(event, "pip")


if __name__ == '__main__':
    unittest.main()
