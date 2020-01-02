package de.mbe.tutorials.aws.lambda;

import de.mbe.tutorials.aws.lambda.pojos.Request;
import de.mbe.tutorials.aws.lambda.pojos.Response;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class Processor {

    public static Response processRequest(final Request request, final String buildAutomationSystem) {

        if (request.isThrowError()) {
            throw new SimpleRuntimeException("Sorry, but the caller wants to me to throw an error");
        }

        final String greeting = String.format("Hello %s %s, this is a Java11 AWS Lambda function built using %s!",
                request.getFirstName(), request.getLastName(), buildAutomationSystem);

        final String result = String.format("Your age in hexadecimal is %s",
                Integer.toHexString(request.getAge()));

        return new Response(greeting, result);
    }

    public static String getRuntimeInfo() {

        final RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        return String.format("%s %s %s", mxBean.getVmVendor(), mxBean.getVmName(), mxBean.getVmVersion());
    }

    public static String getPublicIP() {

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://checkip.amazonaws.com/"))
                .build();

        try {
            final HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200
                    ? String.format("Public IP: %s", response.body().trim())
                    : String.format("Unkown public IP, status code %d received", response.statusCode());

        } catch (IOException e) {
            return String.format("IOExceptioon: %s", e.getMessage());
        } catch (InterruptedException e) {
            return String.format("InterruptedException: %s", e.getMessage());
        }
    }
}
