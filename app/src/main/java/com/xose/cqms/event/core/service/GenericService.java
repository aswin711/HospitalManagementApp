package com.xose.cqms.event.core.service;

import com.xose.cqms.event.core.Constants;
import com.xose.cqms.event.core.modal.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Josekutty on 2/2/2017.
 */
public interface GenericService {

    @GET(Constants.Http.URL_APP_VERSION)
    Call<ApiResponse<String>> getLatestAppVersion(@Path("appname") String appname);
}
