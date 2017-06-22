package com.synnefx.cqms.event.core.service;

import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.ApiResponse;
import com.synnefx.cqms.event.core.modal.ServiceType;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Josekutty on 7/13/2016.
 */
public interface ServiceTypeService {

    @GET(Constants.Http.URL_IMPORT_SERVICES)
    Call<ApiResponse<ServiceType>> getServiceTypes();
}
