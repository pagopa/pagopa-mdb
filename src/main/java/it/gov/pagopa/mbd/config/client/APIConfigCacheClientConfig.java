package it.gov.pagopa.mbd.config.client;

import it.gov.pagopa.mbd.util.apiconfigcache.ApiConfigCacheClientLoggingInterceptor;
import it.gov.pagopa.mbd.util.apiconfigcache.ApiConfigCacheClientResponseErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class APIConfigCacheClientConfig {

    @Value("${client.cache.read-timeout}")
    private Integer readTimeout;

    @Value("${client.cache.connect-timeout}")
    private Integer connectTimeout;

    @Value("${client.cache.url}")
    private String url;

    @Value("${client.cache.api-key}")
    private String apiKey;

    @Bean
    @ConfigurationProperties(prefix = "log.client.cache")
    public RequestResponseLoggingProperties cacheClientLoggingProperties() {
        return new RequestResponseLoggingProperties();
    }

    @Bean
    public it.gov.pagopa.gen.mbd.client.cache.invoker.ApiClient configCacheClient() {
        RequestResponseLoggingProperties clientLoggingProperties = cacheClientLoggingProperties();

        ApiConfigCacheClientLoggingInterceptor clientLogging = new ApiConfigCacheClientLoggingInterceptor(clientLoggingProperties);

        RestTemplate restTemplate = restTemplate();

        List<ClientHttpRequestInterceptor> currentInterceptors = restTemplate.getInterceptors();
        currentInterceptors.add(clientLogging);
        restTemplate.setInterceptors(currentInterceptors);

        restTemplate.setErrorHandler(new ApiConfigCacheClientResponseErrorHandler());

        it.gov.pagopa.gen.mbd.client.cache.invoker.ApiClient client = new it.gov.pagopa.gen.mbd.client.cache.invoker.ApiClient(restTemplate);
        client.setBasePath(url);
        client.setApiKey(apiKey);

        return client;
    }

    private RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // This allows us to read the response more than once - Necessary for debugging.
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(getSimpleClientHttpRequestFactory(restTemplate.getRequestFactory())));

        // disable default URL encoding
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);
        return restTemplate;
    }

    private SimpleClientHttpRequestFactory getSimpleClientHttpRequestFactory(ClientHttpRequestFactory requestFactory) {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = (SimpleClientHttpRequestFactory) requestFactory;
        simpleClientHttpRequestFactory.setConnectTimeout(this.connectTimeout);
        simpleClientHttpRequestFactory.setReadTimeout(this.readTimeout);
        return simpleClientHttpRequestFactory;
    }
}