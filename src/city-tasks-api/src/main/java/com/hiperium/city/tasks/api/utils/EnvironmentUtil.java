package com.hiperium.city.tasks.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiperium.city.tasks.api.logger.HiperiumLogger;
import com.hiperium.city.tasks.api.vo.AuroraSecretsVo;

import java.util.Objects;

public final class EnvironmentUtil {

    private static final HiperiumLogger LOGGER = HiperiumLogger.getLogger(EnvironmentUtil.class);

    private EnvironmentUtil() {
        // Empty constructor.
    }

    public static AuroraSecretsVo getAuroraSecretVO() throws JsonProcessingException {
        String auroraSecret = System.getenv("CITY_TASKS_DB_CLUSTER_SECRET");
        if (Objects.isNull(auroraSecret) || auroraSecret.isBlank()) {
            LOGGER.warn("CITY_TASKS_DB_CLUSTER_SECRET not found. Using defaults.");
            return null;
        }
        return new ObjectMapper().readValue(auroraSecret, AuroraSecretsVo.class);
    }

    public static String getIdpEndpoint() {
        String idpEndpoint = System.getenv("CITY_IDP_ENDPOINT");
        if (Objects.isNull(idpEndpoint) || idpEndpoint.isBlank()) {
            LOGGER.warn("CITY_IDP_ENDPOINT not found. Using defaults.");
        }
        return idpEndpoint;
    }

    public static String getTimeZone() {
        String timeZoneId = System.getenv("CITY_TASKS_TIME_ZONE");
        if (Objects.isNull(timeZoneId) || timeZoneId.isBlank()) {
            LOGGER.warn("CITY_TASKS_TIME_ZONE not found. Using defaults.");
        }
        return timeZoneId;
    }

    public static String getAwsEndpointOverride() {
        String endpointOverride = System.getenv("AWS_ENDPOINT_OVERRIDE");
        if (Objects.isNull(endpointOverride) || endpointOverride.isBlank()) {
            LOGGER.warn("AWS_ENDPOINT_OVERRIDE not found. Using defaults.");
        }
        return endpointOverride;
    }
}
