package com.xose.cqms.event.core.service;

import com.xose.cqms.event.core.Constants;
import com.xose.cqms.event.core.modal.ApiRequest;
import com.xose.cqms.event.core.modal.ApiResponse;
import com.xose.cqms.event.core.modal.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

//import com.xose.cqms.casesheet.core.UsersWrapper;

/**
 * Created by Josekutty on 7/11/2016.
 */
public interface UserService {

    @GET(Constants.Http.URL_USERS_FRAG)
    Call<ApiResponse<User>> getUsers();

    /**
     * The {@link retrofit2.http.Query} values will be transform into query string paramters
     * via Retrofit
     *
     * @param userRequest The users credentials
     * @return A login response.
     */
    @POST(Constants.Http.URL_AUTH_FRAG)
    Call<ApiResponse<String>> authenticate(@Body ApiRequest<User> userRequest);

    @GET(Constants.Http.URL_USER_PROFILE)
    Call<ApiResponse<User>> getProfile();
}
