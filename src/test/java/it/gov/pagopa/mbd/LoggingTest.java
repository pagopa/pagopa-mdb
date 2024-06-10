package it.gov.pagopa.mbd;

import it.gov.pagopa.mbd.config.client.RequestResponseLoggingProperties;
import it.gov.pagopa.mbd.controller.RecoveryController;
import it.gov.pagopa.mbd.util.apiconfigcache.ApiConfigCacheClientLoggingInterceptor;
import it.gov.pagopa.mbd.util.interceptor.AppServerLoggingInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.method.HandlerMethod;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoggingTest {

    @SneakyThrows
    @Test
    public void testClientLogger(){
        HttpRequest httpRequest = mock(HttpRequest.class);
        ClientHttpResponse clientHttpResponse = mock(ClientHttpResponse.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("h1","h1value");

        when(httpRequest.getMethod()).thenReturn(HttpMethod.GET);
        when(httpRequest.getURI()).thenReturn(URI.create("http://localhost"));
        when(httpRequest.getHeaders()).thenReturn(headers);

        when(execution.execute(any(),any())).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(clientHttpResponse.getHeaders()).thenReturn(headers);
        when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream("payload".getBytes(StandardCharsets.UTF_8)));

        RequestResponseLoggingProperties requestResponseLoggingProperties = new RequestResponseLoggingProperties();
        requestResponseLoggingProperties.setRequest(
                new RequestResponseLoggingProperties.Request("h1",true,true,1000,true,false)
        );
        requestResponseLoggingProperties.setResponse(
                new RequestResponseLoggingProperties.Response(true,true,1000,true)
        );

        ApiConfigCacheClientLoggingInterceptor interceptor = new ApiConfigCacheClientLoggingInterceptor(requestResponseLoggingProperties);
        interceptor.intercept(httpRequest,"payload".getBytes(),execution);

    }

    @SneakyThrows
    @Test
    public void testClientLogger2(){
        HttpRequest httpRequest = mock(HttpRequest.class);
        ClientHttpResponse clientHttpResponse = mock(ClientHttpResponse.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("h1","h1value");

        when(httpRequest.getMethod()).thenReturn(HttpMethod.GET);
        when(httpRequest.getURI()).thenReturn(URI.create("http://localhost"));
        when(httpRequest.getHeaders()).thenReturn(headers);

        when(execution.execute(any(),any())).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(clientHttpResponse.getHeaders()).thenReturn(headers);
        when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream("payload".getBytes(StandardCharsets.UTF_8)),new ByteArrayInputStream("payload".getBytes(StandardCharsets.UTF_8)));

        RequestResponseLoggingProperties requestResponseLoggingProperties = new RequestResponseLoggingProperties();
        requestResponseLoggingProperties.setRequest(
                new RequestResponseLoggingProperties.Request(null,true,true,1000,false,false)
        );
        requestResponseLoggingProperties.setResponse(
                new RequestResponseLoggingProperties.Response(true,true,1000,false)
        );

        ApiConfigCacheClientLoggingInterceptor interceptor = new ApiConfigCacheClientLoggingInterceptor(requestResponseLoggingProperties);
        interceptor.intercept(httpRequest,"payload".getBytes(),execution);

    }

    @SneakyThrows
    @Test
    public void testServerLogger(){
        Method method = RecoveryController.class.getMethod("recover", LocalDate.class, LocalDate.class, String[].class);
        HandlerMethod handlerMethod = new HandlerMethod(new RecoveryController(null),method);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        when(httpRequest.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList("h1")));
        when(httpRequest.getHeaders("h1")).thenReturn(Collections.enumeration(Arrays.asList("h1value")));
        when(httpRequest.getQueryString()).thenReturn("a=1&b=2");

        RequestResponseLoggingProperties requestResponseLoggingProperties = new RequestResponseLoggingProperties();
        requestResponseLoggingProperties.setRequest(
                new RequestResponseLoggingProperties.Request(null,true,true,1000,true,true)
        );
        requestResponseLoggingProperties.setResponse(
                new RequestResponseLoggingProperties.Response(true,true,1000,true)
        );

        AppServerLoggingInterceptor interceptor = new AppServerLoggingInterceptor(requestResponseLoggingProperties);
        interceptor.preHandle(httpRequest,httpResponse,handlerMethod);
        interceptor.afterCompletion(httpRequest,httpResponse,handlerMethod,null);
    }

    @SneakyThrows
    @Test
    public void testServerLogger2(){
        Method method = RecoveryController.class.getMethod("recover", LocalDate.class, LocalDate.class, String[].class);
        HandlerMethod handlerMethod = new HandlerMethod(new RecoveryController(null),method);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        when(httpRequest.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList("h1")));
        when(httpRequest.getHeaders("h1")).thenReturn(Collections.enumeration(Arrays.asList("h1value")));

        RequestResponseLoggingProperties requestResponseLoggingProperties = new RequestResponseLoggingProperties();
        requestResponseLoggingProperties.setRequest(
                new RequestResponseLoggingProperties.Request(null,true,true,10,false,false)
        );
        requestResponseLoggingProperties.setResponse(
                new RequestResponseLoggingProperties.Response(true,true,10,false)
        );

        AppServerLoggingInterceptor interceptor = new AppServerLoggingInterceptor(requestResponseLoggingProperties);
        interceptor.preHandle(httpRequest,httpResponse,handlerMethod);
        interceptor.afterCompletion(httpRequest,httpResponse,handlerMethod,null);

    }

}
