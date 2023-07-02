package com.hiperium.city.tasks.events;

import com.hiperium.city.tasks.events.model.EventBridgeCustomEvent;
import com.hiperium.city.tasks.events.utils.FunctionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class ApplicationHandlerTest {

    private static final Logger LOGGER = LogManager.getLogger(ApplicationHandlerTest.class);

    @BeforeAll
    static void init() {
        FunctionUtil.validateAndLoadJsonSchema();
    }

    @Test
    @Order(1)
    void mustUnmarshallEventFromInputStreamTest() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("events/valid-event.json");
        assertNotNull(inputStream);
        EventBridgeCustomEvent event = FunctionUtil.unmarshal(inputStream, EventBridgeCustomEvent.class);
        assertNotNull(event);
    }

    @Test
    @Order(2)
    void mustValidateEventObjectTest() {
        LOGGER.info("mustUnmarshallEventFromInputStreamTest() - START");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("events/valid-event.json");
        assertNotNull(inputStream);
        EventBridgeCustomEvent event = FunctionUtil.unmarshal(inputStream, EventBridgeCustomEvent.class);
        assertNotNull(event);
        FunctionUtil.validateEvent(event);
    }

    @Test
    @Order(3)
    void mustHandleEventWithInvalidDetailTest() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("events/invalid-event-detail.json")) {
            ApplicationHandler handler = new ApplicationHandler();
            IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class,
                    () -> handler.handleRequest(inputStream, null, null), "IllegalArgumentException expected.");
            assertEquals(FunctionUtil.UNMARSHALLING_MESSAGE, expectedException.getMessage());
        }
    }

    @Test
    @Order(4)
    void mustHandleEventWithoutDetailTest() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("events/event-without-detail.json")) {
            ApplicationHandler handler = new ApplicationHandler();
            IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class,
                    () -> handler.handleRequest(inputStream, null, null), "IllegalArgumentException expected.");
            assertEquals(FunctionUtil.VALIDATION_MESSAGE, expectedException.getMessage());
        }
    }
}

