package com.hiperium.city.tasks.events;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.hiperium.city.tasks.events.model.AwsEvent;
import com.hiperium.city.tasks.events.model.TaskExecutionDetail;
import com.hiperium.city.tasks.events.marshaller.Marshaller;

/**
 * Handler for EventBridge invocations of a Lambda function target
 */
public class EventsApplication implements RequestStreamHandler {

    static final String NEW_DETAIL_TYPE = "HelloWorldFunction updated event of %s";

    /**
     * Handles a Lambda Function request
     *
     * @param input   The Lambda Function input stream
     * @param output  The Lambda function output stream
     * @param context The Lambda execution environment context object.
     * @throws IOException
     */
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        AwsEvent<TaskExecutionDetail> awsEvent = Marshaller.unmarshalEvent(input, TaskExecutionDetail.class);
        Object response = this.handleEvent(awsEvent, context);
        Marshaller.marshal(output, response);
    }

    private Object handleEvent(final AwsEvent<TaskExecutionDetail> inputAwsEvent, final Context context) {
        if (Objects.isNull(inputAwsEvent)) {
            throw new IllegalArgumentException("Unable to deserialize lambda input event to AwsEvent<TaskExecutionDetail>. Check that you have the right schema and event source.");
        }
        TaskExecutionDetail detail = inputAwsEvent.getDetail();
        //Developers write your event-driven business logic code here!
        inputAwsEvent.setDetailType(String.format(NEW_DETAIL_TYPE, inputAwsEvent.getDetailType()));
        return inputAwsEvent;
    }
}
