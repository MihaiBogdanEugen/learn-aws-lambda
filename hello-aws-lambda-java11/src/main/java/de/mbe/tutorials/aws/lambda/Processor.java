package de.mbe.tutorials.aws.lambda;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Namespace;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.entities.TraceHeader;
import de.mbe.tutorials.aws.lambda.pojos.Request;
import de.mbe.tutorials.aws.lambda.pojos.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public final class Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);

    public static Response processRequest(final Request request, final String buildAutomationSystem) {
        if (request.isThrowError()) {
            throw new RuntimeException("Sorry, but the caller wants to me to throw an error");
        }

        final String greeting = String.format("Hello %s %s, this is a Java11 AWS Lambda function built using %s!",
                request.getFirstName(), request.getLastName(), buildAutomationSystem);

        final String result = String.format("Your age in hexadecimal is %s",
                Integer.toHexString(request.getAge()));

        return new Response(greeting, result);
    }

    public static String getRuntimeInfo() {
        return AWSXRay.createSubsegment("getRuntimeInfo", () -> {
            final RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
            return String.format("%s %s %s", mxBean.getVmVendor(), mxBean.getVmName(), mxBean.getVmVersion());
        });
    }

    public static String getPublicIP() {
        final Subsegment subsegment = AWSXRay.beginSubsegment("checkip.amazonaws.com");
        subsegment.setNamespace(Namespace.REMOTE.toString());

        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://checkip.amazonaws.com/"))
                    .header(TraceHeader.HEADER_KEY, getTraceHeaderKey(subsegment))
                    .GET()
                    .build();

            subsegment.putHttp("request", getRequestInformation(request));

            final HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            subsegment.putHttp("response", getResponseInformation(response));

            return response.statusCode() / 100 == 2
                    ? String.format("Public IP: %s", response.body().strip())
                    : String.format("Unknown public IP, status code %d received", response.statusCode());

        } catch (IOException e) {
            subsegment.addException(e);
            LOGGER.error(e.getMessage(), e);
            return String.format("IOException: %s", e.getMessage());
        } catch (InterruptedException e) {
            subsegment.addException(e);
            LOGGER.error(e.getMessage(), e);
            return String.format("InterruptedException: %s", e.getMessage());
        } finally {
            subsegment.close();
        }
    }

    private static Map<String, Object> getRequestInformation(final HttpRequest request) {
        final Map<String, Object> requestInformation = new HashMap<>();
        requestInformation.put("url", request.uri().toString());
        requestInformation.put("method", request.method());
        requestInformation.put("traced", false);
        return requestInformation;
    }

    private static <T> Map<String, Object> getResponseInformation(final HttpResponse<T> response) {
        final Map<String, Object> responseInformation = new HashMap<>();
        responseInformation.put("status", response.statusCode());
        return responseInformation;
    }

    private static String getTraceHeaderKey(final Subsegment subsegment) {
        final Segment parentSegment = subsegment.getParentSegment();
        final TraceHeader header = new TraceHeader(parentSegment.getTraceId(),
                parentSegment.isSampled() ? subsegment.getId() : null,
                parentSegment.isSampled() ? TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED);
        return header.toString();
    }
}
