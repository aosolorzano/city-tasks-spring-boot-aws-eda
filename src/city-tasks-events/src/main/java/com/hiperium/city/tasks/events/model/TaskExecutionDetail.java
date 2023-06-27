package com.hiperium.city.tasks.events.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskExecutionDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("instance-id")
    private String instanceId = null;

    @JsonProperty("state")
    private String state = null;

    public TaskExecutionDetail instanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public TaskExecutionDetail state(String state) {
        this.state = state;
        return this;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskExecutionDetail taskExecutionDetail = (TaskExecutionDetail) o;
        return Objects.equals(this.instanceId, taskExecutionDetail.instanceId) &&
                Objects.equals(this.state, taskExecutionDetail.state);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(instanceId, state);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TaskExecutionDetail {\n");
        sb.append("    instanceId: ").append(toIndentedString(instanceId)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
