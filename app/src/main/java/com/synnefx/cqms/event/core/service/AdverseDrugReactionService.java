package com.synnefx.cqms.event.core.service;

import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.ApiRequest;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.core.modal.event.drugreaction.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Josekutty on 2/28/2017.
 */
public interface AdverseDrugReactionService {

    @POST(Constants.Http.URL_PUSH_DRUGREACTION_ERROR)
    Call<ApiResponse> pushRecords(@Body ApiRequest<AdverseDrugEvent> apiRequest);

    @GET(Constants.Http.URL_PULL_DRUGREACTION_ERROR)
    Call<ApiResponse> getRecords();
}
