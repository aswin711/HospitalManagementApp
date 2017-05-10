package com.xose.cqms.event.core.service;

import com.xose.cqms.event.core.Constants;
import com.xose.cqms.event.core.modal.ApiRequest;
import com.xose.cqms.event.core.modal.event.medicationerror.ApiResponse;
import com.xose.cqms.event.core.modal.event.medicationerror.MedicationError;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Josekutty on 2/28/2017.
 */
public interface MedicationErrorService {


    @POST(Constants.Http.URL_PUSH_MEDICATION_ERROR)
    Call<ApiResponse> pushRecords(@Body ApiRequest<MedicationError> apiRequest);

    @GET(Constants.Http.URL_PULL_MEDICATION_ERROR)
    Call<ApiResponse> getRecords();

}
