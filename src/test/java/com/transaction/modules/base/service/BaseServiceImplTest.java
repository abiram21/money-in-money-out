package com.transaction.modules.base.service;

import com.transaction.modules.base.controller.response.PingResponse;
import com.transaction.modules.base.service.BaseServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseServiceImplTest {

    @InjectMocks
    private BaseServiceImpl baseService;

    @Test
    public void ping() {
        PingResponse pingResponse = baseService.ping();
        Assertions.assertNotNull(pingResponse.getServerTime());
    }
}