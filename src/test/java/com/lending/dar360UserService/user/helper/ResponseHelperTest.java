package com.lending.dar360UserService.user.helper;

import com.lending.dar360UserService.user.constant.AppStatusConstant;
import org.junit.Assert;
import org.junit.Test;

public class ResponseHelperTest {

    @Test
    public void getResponse() {
        Assert.assertNotNull(ResponseHelper.getResponse(AppStatus.ofOk("ok", new Object())));
    }

    @Test
    public void getHttpStatus() {
        Assert.assertNotNull(ResponseHelper.getHttpStatus(AppStatusConstant.Code.ok));
        Assert.assertNotNull(ResponseHelper.getHttpStatus(AppStatusConstant.Code.internalSeverError));
        Assert.assertNull(ResponseHelper.getHttpStatus(""));
        Assert.assertNull(ResponseHelper.getHttpStatus("abc"));
    }
}