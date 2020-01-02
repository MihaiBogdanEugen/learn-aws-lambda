package de.mbe.tutorials.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.lambda.pojos.Request;
import de.mbe.tutorials.aws.lambda.pojos.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static de.mbe.tutorials.aws.lambda.Processor.*;

public class FnHelloHandlerWithStreams implements RequestStreamHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FnHelloHandlerWithStreams.class);
    private static final double STATIC_RANDOM = Math.random();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final double constructorRandom;

    public FnHelloHandlerWithStreams() {
        this.constructorRandom = Math.random();
    }

    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context) throws IOException {

        LOGGER.info(getRuntimeInfo());
        LOGGER.info(getPublicIP());

        LOGGER.info("{} called {} has {} seconds to live",
                System.getenv("AWS_EXECUTION_ENV"), context.getFunctionName(), context.getRemainingTimeInMillis() / 1000);

        LOGGER.info("Static value : {}, constructor value: {}, invocation value: {}",
                STATIC_RANDOM, this.constructorRandom, Math.random());

        final Request request = OBJECT_MAPPER.readValue(input, Request.class);
        final Response response = processRequest(request, System.getenv("BUILD_AUTOMATION_SYSTEM"));

        OBJECT_MAPPER.writeValue(output, response);

        output.flush();
        output.close();
    }
}
