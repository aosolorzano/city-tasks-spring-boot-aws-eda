package com.hiperium.city.tasks.events;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hiperium.city.tasks.events.model.TaskExecutionDetail;
import org.junit.Test;

import com.hiperium.city.tasks.events.model.AwsEvent;
import com.hiperium.city.tasks.events.marshaller.Marshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EventsApplicationTest {
    private static final String MY_DETAIL_TYPE = "myDetailType";

    @Test
    public void successfulResponse() throws IOException {
        AwsEvent<TaskExecutionDetail> awsEvent =
                new AwsEvent<TaskExecutionDetail>()
                        .detail(new TaskExecutionDetail())
                        .detailType(MY_DETAIL_TYPE);
        ByteArrayOutputStream handlerOutput = new ByteArrayOutputStream();

        EventsApplication app = new EventsApplication();
        app.handleRequest(toInputStream(awsEvent), handlerOutput, null);

        AwsEvent<TaskExecutionDetail> responseAwsEvent = fromOutputStream(handlerOutput);
        assertNotNull(responseAwsEvent);
        assertEquals(String.format(EventsApplication.NEW_DETAIL_TYPE, MY_DETAIL_TYPE), responseAwsEvent.getDetailType());
    }

    private InputStream toInputStream(AwsEvent<TaskExecutionDetail> awsEvent) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Marshaller.marshal(output, awsEvent);
        return new ByteArrayInputStream(output.toByteArray());
    }

    private AwsEvent<TaskExecutionDetail> fromOutputStream(ByteArrayOutputStream outputStream) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return Marshaller.unmarshalEvent(inputStream, TaskExecutionDetail.class);
    }
}

