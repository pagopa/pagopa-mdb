package it.gov.pagopa.mbd.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String SERVICE_CODE_APP = "WIC";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    public static final String MDC_START_TIME = "startTime";
    public static final String MDC_CALL_TYPE = "callType";
    public static final String MDC_EVENT_CATEGORY = "eventCategory";
    public static final String MDC_EVENT_SUB_CATEGORY = "eventSubCategory";
    public static final String MDC_STATUS = "status";
    public static final String MDC_STATUS_CODE = "httpCode";
    public static final String MDC_EXECUTION_TIME = "executionTime";
    public static final String MDC_ERROR_CODE = "errorCode";
    public static final String MDC_ERROR_TITLE = "errorTitle";
    public static final String MDC_ERROR_DETAIL = "errorDetail";
    public static final String MDC_OPERATION_ID = "operationId";
    public static final String MDC_BUSINESS_PROCESS = "businessProcess";
    public static final String MDC_REQUEST_ID = "requestId";

    public static final String MDC_CLIENT_OPERATION_ID = "clientOperationId";
    public static final String MDC_CLIENT_SERVICE_ID = "clientServiceId";
    public static final String MDC_CLIENT_EXECUTION_TIME = "clientExecutionTime";

}
