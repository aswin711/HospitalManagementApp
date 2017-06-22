package com.synnefx.cqms.event.core.service;

import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.ApiRequest;
import com.synnefx.cqms.event.core.modal.event.incident.ApiResponse;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Josekutty on 2/10/2017.
 */
public interface IncidentReportService {

    @POST(Constants.Http.URL_PUSH_INCIDENT)
    Call<ApiResponse> pushRecords(@Body ApiRequest<IncidentReport> apiRequest);

    @GET(Constants.Http.URL_PULL_INCIDENT)
    Call<ApiResponse> getRecords();
}
