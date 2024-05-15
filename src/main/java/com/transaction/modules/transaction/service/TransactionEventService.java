package com.transaction.modules.transaction.service;


import com.transaction.modules.transaction.controller.request.AuthorizationRequest;
import com.transaction.modules.transaction.controller.request.LoadRequest;
import com.transaction.modules.transaction.controller.response.AuthorizationResponse;
import com.transaction.modules.transaction.controller.response.LoadResponse;

public interface TransactionEventService {

    LoadResponse load(LoadRequest loadRequest, String messageId);

    AuthorizationResponse authorize(AuthorizationRequest authorizationRequest, String messageId);

}
