package com.hiperium.city.tasks.api.scheduler.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiperium.city.tasks.api.dto.TaskEventDto;
import com.hiperium.city.tasks.api.logger.HiperiumLogger;
import com.hiperium.city.tasks.api.model.Task;
import com.hiperium.city.tasks.api.repository.DeviceRepository;
import com.hiperium.city.tasks.api.repository.TaskRepository;
import com.hiperium.city.tasks.api.utils.SchedulerUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

import java.util.Objects;

@Component
public class JobExecution implements Job {

    private static final HiperiumLogger LOGGER = HiperiumLogger.getLogger(JobExecution.class);

    private final TaskRepository taskRepository;
    private final DeviceRepository deviceRepository;
    private final EventBridgeClient eventBridgeClient;

    public JobExecution(TaskRepository taskRepository, DeviceRepository deviceRepository,
                        EventBridgeClient eventBridgeClient) {
        this.taskRepository = taskRepository;
        this.deviceRepository = deviceRepository;
        this.eventBridgeClient = eventBridgeClient;
    }

    @Override
    public void execute(final JobExecutionContext context) {
        LOGGER.debug("execute() - START");
        final String jobId = context.getJobDetail().getJobDataMap().getString(SchedulerUtil.TASK_JOB_ID_DATA_KEY);
        Mono.just(jobId)
                .map(this.taskRepository::findByJobId)
                .flatMap(this.deviceRepository::updateStatusByTaskOperation)
                .doOnNext(this::triggerCustomEvent)
                .subscribe(
                        result -> LOGGER.info("Job executed successfully: {}", jobId),
                        error -> LOGGER.error("Error executing job: {}.  Error: {}", jobId, error.getMessage())
                );
    }

    private void triggerCustomEvent(final Task task) {
        ObjectMapper objectMapper = new ObjectMapper();
        TaskEventDto taskEventDto = TaskEventDto.builder()
                .taskId(task.getId())
                .deviceId(task.getDeviceId())
                .deviceOperation(task.getDeviceOperation())
                .build();
        PutEventsRequestEntry entry = createRequestEntry(objectMapper, taskEventDto);
        PutEventsRequest eventRequest = PutEventsRequest.builder()
                .entries(entry)
                .build();

        this.eventBridgeClient.putEvents(eventRequest);
        PutEventsResponse result = this.eventBridgeClient.putEvents(eventRequest);
        for (PutEventsResultEntry resultEntry : result.entries()) {
            if (Objects.nonNull(resultEntry.eventId())) {
                LOGGER.info("Event ID: {} sent successfully to EventBridge.", resultEntry.eventId());
            } else {
                LOGGER.error("Error sending event to EventBridge. Error Code: {}. Error message: {}.",
                        resultEntry.errorCode(), resultEntry.errorMessage());
            }
        }
    }

    private static PutEventsRequestEntry createRequestEntry(ObjectMapper objectMapper, TaskEventDto taskEventDto) {
        PutEventsRequestEntry entry;
        try {
            entry = PutEventsRequestEntry.builder()
                    .source("com.hiperium.city.tasks")
                    .detailType("TaskExecution")
                    .detail(objectMapper.writeValueAsString(taskEventDto))
                    .build();
        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing Task Event: {}.", e.getMessage());
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return entry;
    }
}

