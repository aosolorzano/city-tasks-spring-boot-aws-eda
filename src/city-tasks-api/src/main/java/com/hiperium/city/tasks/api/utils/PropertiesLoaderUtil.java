package com.hiperium.city.tasks.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hiperium.city.tasks.api.vo.AuroraSecretsVo;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Objects;

@Slf4j
public final class PropertiesLoaderUtil {

    public static final String AWS_ENDPOINT_OVERRIDE = "aws.endpoint-override";
    private static final String JDBC_SQL_CONNECTION = "jdbc:postgresql://{0}:{1}/{2}";

    private PropertiesLoaderUtil() {
        // Private constructor.
    }

    public static void loadProperties() throws JsonProcessingException {
        setDatasourceConnection();
        setIdentityProviderEndpoint();
        setApplicationTimeZone();
        setAwsEndpointOverride();
    }

    public static void setDatasourceConnection() throws JsonProcessingException {
        AuroraSecretsVo auroraSecretVO = EnvironmentUtil.getAuroraSecretVO();
        if (Objects.nonNull(auroraSecretVO)) {
            String sqlConnection = MessageFormat.format(JDBC_SQL_CONNECTION, auroraSecretVO.host(),
                    auroraSecretVO.port(), auroraSecretVO.dbname());
            log.debug("JDBC Connection: {}", sqlConnection);
            // Set Datasource connection for JPA.
            System.setProperty("spring.datasource.url", sqlConnection);
            System.setProperty("spring.datasource.username", auroraSecretVO.username());
            System.setProperty("spring.datasource.password", auroraSecretVO.password());
            // Set Datasource connection for Quartz.
            System.setProperty("spring.quartz.properties.org.quartz.dataSource.cityTasksQuartzDS.URL", sqlConnection);
            System.setProperty("spring.quartz.properties.org.quartz.dataSource.cityTasksQuartzDS.user", auroraSecretVO.username());
            System.setProperty("spring.quartz.properties.org.quartz.dataSource.cityTasksQuartzDS.password", auroraSecretVO.password());
        }
    }

    public static void setIdentityProviderEndpoint() {
        String idpEndpoint = EnvironmentUtil.getIdpEndpoint();
        if (Objects.nonNull(idpEndpoint) && !idpEndpoint.isEmpty()) {
            log.debug("IdP URI: {}", idpEndpoint);
            System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", idpEndpoint);
        }
    }

    public static void setApplicationTimeZone() {
        String timeZoneId = EnvironmentUtil.getTimeZone();
        if (Objects.nonNull(timeZoneId) && !timeZoneId.isEmpty()) {
            log.debug("Time Zone: {}", timeZoneId);
            System.setProperty("city.tasks.time.zone", timeZoneId);
        }
    }

    public static void setAwsEndpointOverride() {
        String endpointOverride = EnvironmentUtil.getAwsEndpointOverride();
        if (Objects.nonNull(endpointOverride) && !endpointOverride.isEmpty()) {
            log.debug("AWS Endpoint-Override: {}", endpointOverride);
            System.setProperty(AWS_ENDPOINT_OVERRIDE, endpointOverride);
        }
    }
}
