package it.gov.pagopa.mbd.util;

import it.gov.pagopa.mbd.exception.AppError;
import it.gov.pagopa.mbd.exception.AppException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtility {

    /**
     * @param value value to deNullify.
     * @return return empty string if value is null
     */
    public static String deNull(String value) {
        return Optional.ofNullable(value).orElse("");
    }

    /**
     * @param value value to deNullify.
     * @return return empty string if value is null
     */
    public static String deNull(Object value) {
        return Optional.ofNullable(value).orElse("").toString();
    }

    /**
     * @param value value to deNullify.
     * @return return false if value is null
     */
    public static Boolean deNull(Boolean value) {
        return Optional.ofNullable(value).orElse(false);
    }

    public static String getExecutionTime(String startTime) {
        if (startTime != null) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - Long.parseLong(startTime);
            return String.valueOf(executionTime);
        }
        return "-";
    }


    public static String getAppCode(AppError error) {
        return String.format("%s-%s", Constants.SERVICE_CODE_APP, error.httpStatus);
    }

    public static String constructUrl(String protocol, String hostname, int port, String path, String query, String fragment) {
        try {
            String pathMod = null;
            if( null != path ) {
                pathMod = path.startsWith("/") ? path : ("/" + path);
            }

            return new URI(
                    protocol.toLowerCase(),
                    null,
                    hostname,
                    port,
                    pathMod,
                    query,
                    fragment).toString();
        } catch (Exception e) {
            throw new AppException(AppError.INTERNAL_SERVER_ERROR);
        }
    }

    public static String getConfigKeyValueCache(Map<String, it.gov.pagopa.gen.mbd.client.cache.model.ConfigurationKeyDto> configurations, String key) {
        try {
            return configurations.get(key).getValue();
        } catch ( NullPointerException e) {
            throw new AppException(AppError.INTERNAL_SERVER_ERROR, "ConfigurationKey '" + key + "' not found in cache");
        }
    }


}