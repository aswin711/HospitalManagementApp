package com.synnefx.cqms.event.core.service;

import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.ApiAuthResponse;
import com.synnefx.cqms.event.core.modal.ApiRequest;
import com.synnefx.cqms.event.core.modal.ApiResponse;
import com.synnefx.cqms.event.core.modal.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

//import com.synnefx.cqms.casesheet.core.UsersWrapper;

/**
 * Created by Josekutty on 7/11/2016.
 */
public interface UserService {


    /**
     * The {@link retrofit2.http.Query} values will be transform into query string paramters
     * via Retrofit
     *
     * @param userRequest The users credentials
     * @return A login response.
     */
    @POST(Constants.Http.URL_AUTH_FRAG)
    Call<ApiResponse<ApiAuthResponse>> authenticate(@Body ApiRequest<User> userRequest);

    @GET(Constants.Http.URL_USER_PROFILE)
    Call<ApiResponse<User>> getProfile();
}
