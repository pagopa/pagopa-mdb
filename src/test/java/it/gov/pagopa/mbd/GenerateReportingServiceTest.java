package it.gov.pagopa.mbd;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mbd.repository.BizEventRepository;
import it.gov.pagopa.mbd.service.ConfigCacheService;
import it.gov.pagopa.mbd.util.JaxbElementUtil;
import it.gov.pagopa.wispconverter.controller.model.ReceiptRequest;
import it.gov.pagopa.wispconverter.exception.PaaInviaRTException;
import it.gov.pagopa.wispconverter.repository.CacheRepository;
import it.gov.pagopa.wispconverter.repository.RPTRequestRepository;
import it.gov.pagopa.wispconverter.repository.RTRequestRepository;
import it.gov.pagopa.wispconverter.repository.ReEventRepository;
import it.gov.pagopa.wispconverter.repository.model.RPTRequestEntity;
import it.gov.pagopa.wispconverter.service.ConfigCacheService;
import it.gov.pagopa.wispconverter.service.PaaInviaRTService;
import it.gov.pagopa.wispconverter.service.PaaInviaRTServiceBusService;
import it.gov.pagopa.wispconverter.service.model.ReceiptDto;
import it.gov.pagopa.wispconverter.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class GenerateReportingServiceTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @Autowired private ConfigCacheService configCacheService;

    @MockBean
    private ApplicationStartup applicationStartup;
    @MockBean
    private BizEventRepository bizEventRepository;

    @Test
    void success_positive() throws Exception {
        String station = "mystation";
        org.springframework.test.util.ReflectionTestUtils.setField(configCacheService, "configData",TestUtils.configData(station));

        when(rptRequestRepository.findById(any())).thenReturn(
                Optional.of(
                        RPTRequestEntity.builder().primitive("nodoInviaRPT")
                                .payload(
                                        TestUtils.zipAndEncode(TestUtils.getRptPayload(false,station,"100.00","datispec"))
                                ).build()
                )
        );
        when(cacheRepository.read(any(),any())).thenReturn("asdsad");

        mvc.perform(MockMvcRequestBuilders.post("/receipt/ok")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReceiptRequest(getPaSendRTPayload()))))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(
                        (result) -> {
                            assertNotNull(result);
                            assertNotNull(result.getResponse());
                        });


        verify(reEventRepository,times(5)).save(any());
    }

    @Test
    void success_negative() throws Exception {
        String station = "mystation";
        org.springframework.test.util.ReflectionTestUtils.setField(configCacheService, "configData",TestUtils.configData(station));

        when(rptRequestRepository.findById(any())).thenReturn(Optional.of(RPTRequestEntity
                .builder()
                        .id(UUID.randomUUID().toString())
                        .primitive("nodoInviaRPT")
                        .payload(TestUtils.zipAndEncode(TestUtils.getRptPayload(false,"mystation","10.00","dati")))
                .build()));
        when(cacheRepository.read(any(),any())).thenReturn("wisp_nav2iuv_dominio");

        ReceiptDto[] receiptDtos = {
                new ReceiptDto("token", "dominio", "iuv")
        };
        mvc.perform(MockMvcRequestBuilders.post("/receipt/ko")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReceiptRequest(objectMapper.writeValueAsString(receiptDtos)))))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(
                        (result) -> {
                            assertNotNull(result);
                            assertNotNull(result.getResponse());
                        });

        verify(reEventRepository,times(5)).save(any());
    }

    @Test
    void error_send_rt() throws Exception {
        String station = "mystation";
        org.springframework.test.util.ReflectionTestUtils.setField(configCacheService, "configData",TestUtils.configData(station));

        when(rptRequestRepository.findById(any())).thenReturn(
                Optional.of(
                        RPTRequestEntity.builder().primitive("nodoInviaRPT")
                                .payload(
                                        TestUtils.zipAndEncode(TestUtils.getRptPayload(false,station,"100.00","datispec"))
                                ).build()
                )
        );
        when(cacheRepository.read(any(),any())).thenReturn("asdsad");
        doThrow(new PaaInviaRTException("PAA_ERRORE_RESPONSE","PAA_ERRORE_RESPONSE","Errore PA")).doNothing().when(paaInviaRTService).send(anyString(), anyString());

        mvc.perform(MockMvcRequestBuilders.post("/receipt/ok")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReceiptRequest(getPaSendRTPayload()))))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(
                        (result) -> {
                            assertNotNull(result);
                            assertNotNull(result.getResponse());
                        });

        verify(paaInviaRTService, times(1)).send(anyString(), anyString());
        verify(reEventRepository,times(6)).save(any());
    }

    @Test
    void error_send_rt2() throws Exception {
        String station = "mystation";
        org.springframework.test.util.ReflectionTestUtils.setField(configCacheService, "configData",TestUtils.configData(station));

        when(rptRequestRepository.findById(any())).thenReturn(
                Optional.of(
                        RPTRequestEntity.builder().primitive("nodoInviaRPT")
                                .payload(
                                        TestUtils.zipAndEncode(TestUtils.getRptPayload(false,station,"100.00","datispec"))
                                ).build()
                )
        );
        when(cacheRepository.read(any(),any())).thenReturn("asdsad");
        doAnswer((i) -> {
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        }).when(paaInviaRTService).send(anyString(), anyString());
        mvc.perform(MockMvcRequestBuilders.post("/receipt/ok")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReceiptRequest(getPaSendRTPayload()))))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(
                        (result) -> {
                            assertNotNull(result);
                            assertNotNull(result.getResponse());
                        });

        verify(paaInviaRTService, times(1)).send(anyString(), anyString());
        verify(reEventRepository,times(5)).save(any());
    }
     
}