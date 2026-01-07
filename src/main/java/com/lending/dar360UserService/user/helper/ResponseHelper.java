package com.lending.dar360UserService.user.helper;

import com.lending.dar360UserService.user.constant.AppStatusConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHelper {
    public static <T> ResponseEntity getResponse(AppStatus appStatus) {
        String code = appStatus.getCode();
        return ResponseEntity.status(ResponseHelper.getHttpStatus(code)).body(appStatus.getData());
    }

    public static HttpStatus getHttpStatus(String code) {
        if(StringUtils.isEmpty(code)) return null;

        HttpStatus httpStatus = switch (code) {
            case AppStatusConstant.Code.ok -> HttpStatus.OK;
            case AppStatusConstant.Code.internalSeverError -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> null;
        };

        return httpStatus;
    }
}
