package com.hiperium.city.tasks.api.service.impl;

import com.hiperium.city.tasks.api.exception.ResourceNotFoundException;
import com.hiperium.city.tasks.api.model.Task;
import com.hiperium.city.tasks.api.repository.TaskRepository;
import com.hiperium.city.tasks.api.scheduler.TaskScheduler;
import com.hiperium.city.tasks.api.service.TaskService;
import com.hiperium.city.tasks.api.utils.TaskUtil;
import com.hiperium.city.tasks.api.utils.enums.EnumResourceError;
import com.hiperium.city.tasks.api.utils.enums.EnumTaskStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZonedDateTime;
import java.util.Objects;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskScheduler taskScheduler;

    public TaskServiceImpl(TaskRepository taskRepository, TaskScheduler taskScheduler) {
        this.taskRepository = taskRepository;
        this.taskScheduler = taskScheduler;
    }

    public Mono<Task> create(final Task task) {
        // TODO: Validate if the task already exists with the same values.
        return Mono.just(task)
                .doOnNext(TaskServiceImpl::setCreateDefaultValues)
                .doOnNext(this.taskRepository::save)
                .doOnNext(this.taskScheduler::scheduleJob);
    }

    public Mono<Task> update(final Task modifiedTask) {
        return this.findById(modifiedTask)
                .doOnNext(actualTask -> this.setUpdateDefaultValues(modifiedTask, actualTask))
                .doOnNext(actualTask -> BeanUtils.copyProperties(modifiedTask, actualTask))
                .doOnNext(this.taskRepository::save)
                .doOnNext(this::rescheduleTaskJob);

    }

    // TODO: Implement a method that updates the status of a task. Depending on the status, the task will be scheduled or unscheduled.

    public Mono<Task> delete(final Task task) {
        return this.findById(task)
                .doOnNext(this.taskScheduler::unscheduleJob)
                .doOnNext(this.taskRepository::delete)
                .doOnNext(deletedTask -> {
                    deletedTask.setId(null);
                    deletedTask.setStatus(null);
                    deletedTask.setJobId(null);
                    deletedTask.setCreatedAt(null);
                    deletedTask.setUpdatedAt(null);
                });
    }

    private Mono<Task> findById(final Task task) {
        return Mono.fromSupplier(() -> this.taskRepository.findById(task.getId()))
                .filter(Objects::nonNull)
                .map(taskOptional -> taskOptional
                        .orElseThrow(() -> new ResourceNotFoundException(EnumResourceError.TASK_NOT_FOUND, task.getId())))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static void setCreateDefaultValues(Task scheduledTask) {
        scheduledTask.setJobId(TaskUtil.generateJobId());
        scheduledTask.setStatus(EnumTaskStatus.ACT);
        scheduledTask.setCreatedAt(ZonedDateTime.now());
        scheduledTask.setUpdatedAt(ZonedDateTime.now());
        if (Objects.isNull(scheduledTask.getExecuteUntil())) {
            scheduledTask.setExecuteUntil(ZonedDateTime.now()
                    .plusYears(1).withMonth(12).withDayOfMonth(31)
                    .withHour(23).withMinute(59).withSecond(59));
        }
    }

    private void setUpdateDefaultValues(Task modifiedTask, Task actualTask) {
        modifiedTask.setJobId(actualTask.getJobId());
        modifiedTask.setStatus(actualTask.getStatus());
        modifiedTask.setCreatedAt(actualTask.getCreatedAt());
        modifiedTask.setUpdatedAt(ZonedDateTime.now());
    }

    private void rescheduleTaskJob(Task actualTask) {
        if (actualTask.getStatus().equals(EnumTaskStatus.ACT)) {
            this.taskScheduler.rescheduleJob(actualTask);
        }
    }
}
