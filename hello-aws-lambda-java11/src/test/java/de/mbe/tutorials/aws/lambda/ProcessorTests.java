package de.mbe.tutorials.aws.lambda;

import de.mbe.tutorials.aws.lambda.pojos.Request;
import de.mbe.tutorials.aws.lambda.pojos.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static de.mbe.tutorials.aws.lambda.Processor.processRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProcessorTests {
    
    @Test
    void testProcessRequestNoError() throws IOException, InterruptedException {

        final Request request = new Request("Bogdan-Eugen", "Mihai", 34);
        final Response response = processRequest(request, "anything but Apache Ant");
        assertEquals("Hello Bogdan-Eugen Mihai, this is a Java11 AWS Lambda function built using anything but Apache Ant!", response.getGreeting());
        assertEquals("Your age in hexadecimal is 22", response.getResult());
    }

    @Test
    void testProcessRequestThrowError() {

        final Request request = new Request("Bogdan-Eugen", "Mihai", 34, true);

        assertThrows(SimpleRuntimeException.class, () -> processRequest(request, "anything but Apache Ant"));
    }
}
