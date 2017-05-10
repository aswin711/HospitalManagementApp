package com.xose.cqms.event.core.service;

import com.xose.cqms.event.core.Constants;
import com.xose.cqms.event.core.modal.ApiResponse;
import com.xose.cqms.event.core.modal.Unit;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Josekutty on 8/5/2016.
 */
public interface UnitService {

    @GET(Constants.Http.URL_IMPORT_UNITS)
    Call<ApiResponse<Unit>> getUnits();
}
