package com.transaction.modules.base.controller;

import com.transaction.modules.base.controller.response.PingResponse;
import com.transaction.modules.base.service.BaseService;
import com.transaction.util.response.ErrorResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1")
public class BaseController {

    @Autowired
    private BaseService baseService;

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        try {
            PingResponse pingResponse = baseService.ping();
            return ResponseEntity.ok(pingResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(500, "Internal Server Error."));
        }
    }
}
