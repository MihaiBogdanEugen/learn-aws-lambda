package de.mbe.tutorials.aws.lambda.pojos;

public class Response {

    private String greeting;
    private String result;

    public Response() { }

    public Response(final String greeting, final String result) {
        this.greeting = greeting;
        this.result = result;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(final String greeting) {
        this.greeting = greeting;
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }
}
