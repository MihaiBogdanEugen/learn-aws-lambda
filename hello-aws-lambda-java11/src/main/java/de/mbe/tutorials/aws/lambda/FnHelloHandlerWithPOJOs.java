package de.mbe.tutorials.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import de.mbe.tutorials.aws.lambda.pojos.Request;
import de.mbe.tutorials.aws.lambda.pojos.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.mbe.tutorials.aws.lambda.Processor.*;

public class FnHelloHandlerWithPOJOs implements RequestHandler<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FnHelloHandlerWithPOJOs.class);
    private static final double STATIC_RANDOM = Math.random();

    private final double constructorRandom;

    public FnHelloHandlerWithPOJOs() {
        this.constructorRandom = Math.random();
    }

    @Override
    public Response handleRequest(final Request request, final Context context) {

        LOGGER.info(getRuntimeInfo());
        LOGGER.info(getPublicIP());

        LOGGER.info("{} called {} has {} seconds to live",
                System.getenv("AWS_EXECUTION_ENV"), context.getFunctionName(), context.getRemainingTimeInMillis() / 1000);

        LOGGER.info("Static value : {}, constructor value: {}, invocation value: {}",
                STATIC_RANDOM, this.constructorRandom, Math.random());

        return processRequest(request, System.getenv("BUILD_AUTOMATION_SYSTEM"));
    }
}
