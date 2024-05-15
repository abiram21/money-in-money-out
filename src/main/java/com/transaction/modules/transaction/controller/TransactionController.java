package com.transaction.modules.transaction.controller;

import com.transaction.modules.transaction.controller.request.AuthorizationRequest;
import com.transaction.modules.transaction.controller.request.LoadRequest;
import com.transaction.modules.transaction.controller.response.AuthorizationResponse;
import com.transaction.modules.transaction.controller.response.LoadResponse;
import com.transaction.modules.transaction.service.TransactionEventService;
import com.transaction.util.response.ErrorResponseModel;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transaction")
public class TransactionController {

    @Autowired
    private TransactionEventService transactionEventService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
    })
    @PutMapping("/load/{messageId}")
    public ResponseEntity<?> load(@Valid @RequestBody LoadRequest loadRequest, @PathVariable String messageId) {
        try {
            LoadResponse loadResponse = transactionEventService.load(loadRequest, messageId);
            return ResponseEntity.ok(loadResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseModel(400, e.getMessage()));
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
    })
    @PutMapping("/authorize/{messageId}")
    public ResponseEntity<?> authorize(@Valid @RequestBody AuthorizationRequest authorizationRequest, @PathVariable String messageId) {
        try {
            AuthorizationResponse authorizationResponse = transactionEventService.authorize(authorizationRequest, messageId);
            return ResponseEntity.ok(authorizationResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseModel(400, e.getMessage()));
        }
    }
}

