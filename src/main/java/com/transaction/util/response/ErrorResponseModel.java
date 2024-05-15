package com.transaction.util.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorResponseModel {

    private String message;

    private Integer code;

    public ErrorResponseModel(Integer code, String message) {
        setCode(code);
        setMessage(message);
    }

}
