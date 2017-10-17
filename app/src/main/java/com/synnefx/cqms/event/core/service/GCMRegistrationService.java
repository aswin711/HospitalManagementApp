package com.synnefx.cqms.event.core.service;

import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.ApiRequest;
import com.synnefx.cqms.event.core.modal.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Josekutty on 8/16/2016.
 */
public interface GCMRegistrationService {
    @POST(Constants.Http.URL_GCM_REG)
    Call<ApiResponse<String>> register(@Body ApiRequest<GCMRequest> gcmRequest);
}
