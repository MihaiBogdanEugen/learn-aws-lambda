"use strict";

const app = require("../fnHello.js");
const chai = require("chai");
const expect = chai.expect;

describe("processRequest works when throwError is false", function () {
    it("testProcessRequestNoError", () => {
        const event = {
            "firstName": "Bogdan-Eugen",
            "lastName": "Mihai",
            "age": 34,
            "throwError": false
        }
        const response = app.exportedProcessRequest(event, "npm")

        expect(response).to.be.an("object");
        expect(response.greeting).to.be.equal("Hello Bogdan-Eugen Mihai, this is a Nodejs12.x AWS Lambda function developed using npm!");
        expect(response.result).to.be.equal("Your age in hexadecimal is 22");
    });
});

describe("processRequest throws error when throwError is true", function () {
    it("testProcessRequestThrowError", () => {
        const event = {
            "firstName": "Bogdan-Eugen",
            "lastName": "Mihai",
            "age": 34,
            "throwError": true
        }

        expect(function() {
            app.exportedProcessRequest(event, "npm")
        }).to.throw(Error).with.property("message", "Sorry, but the caller wants to me to throw an error");
    });
});
