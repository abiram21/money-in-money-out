package com.transaction.modules.base.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class PingResponse {
    private LocalDateTime serverTime;

    public PingResponse(LocalDateTime serverTime) {
        setServerTime(serverTime);
    }
}
