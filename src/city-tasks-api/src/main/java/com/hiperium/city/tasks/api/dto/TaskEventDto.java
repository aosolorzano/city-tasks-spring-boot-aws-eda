package com.hiperium.city.tasks.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hiperium.city.tasks.api.utils.enums.EnumDeviceOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEventDto {

    @JsonProperty("task-id")
    private Long taskId;

    @JsonProperty("device-id")
    private String deviceId;

    @JsonProperty("device-operation")
    private EnumDeviceOperation deviceOperation;
}
