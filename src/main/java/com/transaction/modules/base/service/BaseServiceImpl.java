package com.transaction.modules.base.service;

import com.transaction.modules.base.controller.response.PingResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BaseServiceImpl implements BaseService {
    @Override
    public PingResponse ping() {
        return new PingResponse(LocalDateTime.now());
    }
}
