package com.synnefx.cqms.event.core;

import com.squareup.otto.Bus;
import com.synnefx.cqms.event.authenticator.ApiKeyProvider;
import com.synnefx.cqms.event.core.modal.ApiAuthResponse;
import com.synnefx.cqms.event.core.modal.ApiRequest;
import com.synnefx.cqms.event.core.modal.ApiResponse;
import com.synnefx.cqms.event.core.modal.AuthHeader;
import com.synnefx.cqms.event.core.modal.IncidentType;
import com.synnefx.cqms.event.core.modal.Specialty;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.core.modal.User;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.synnefx.cqms.event.core.service.AdverseDrugReactionService;
import com.synnefx.cqms.event.core.service.GCMRegistrationService;
import com.synnefx.cqms.event.core.service.GCMRequest;
import com.synnefx.cqms.event.core.service.GenericService;
import com.synnefx.cqms.event.core.service.IncidentReportService;
import com.synnefx.cqms.event.core.service.IncidentTypeService;
import com.synnefx.cqms.event.core.service.MedicationErrorService;
import com.synnefx.cqms.event.core.service.SpecialtyService;
import com.synnefx.cqms.event.core.service.UnitService;
import com.synnefx.cqms.event.core.service.UserService;
import com.synnefx.cqms.event.events.NetworkErrorEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

//import retrofit.RequestInterceptor;
//import retrofit.RestAdapter;

/**
 * Bootstrap API service
 */
public class BootstrapService {

    private Bus bus;

    //private RestAdapter.Builder restAdapterBuilder;

    private Retrofit.Builder retrofitBuilder;

    private UserAgentProvider userAgentProvider;

    private ApiKeyProvider apiKeyProvider;

    private String accessToken;

    private String deviceToken;

    /**
     * Create bootstrap service
     * Default CTOR
     */
    public BootstrapService() {
    }

    /**
     * Create bootstrap service
     */


    public BootstrapService(Retrofit.Builder retrofitBuilder, UserAgentProvider userAgentProvider, ApiKeyProvider apiKeyProvider, Bus bus) {
        this.retrofitBuilder = retrofitBuilder;
        this.userAgentProvider = userAgentProvider;
        this.apiKeyProvider = apiKeyProvider;
        this.bus = bus;
    }

    public BootstrapService(Retrofit.Builder retrofitBuilder, UserAgentProvider userAgentProvider, String authKey, String deviceID, Bus bus) {
        this.retrofitBuilder = retrofitBuilder;
        this.userAgentProvider = userAgentProvider;
        this.deviceToken = deviceID;
        this.accessToken = authKey;
        this.bus = bus;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null);
    }

    public <S> S createAuthenticatedService(Class<S> serviceClass) {
        return createService(serviceClass, this.accessToken);
    }

    public <S> S createService(Class<S> serviceClass, final String accessToken) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(15, TimeUnit.SECONDS);
        httpClient.readTimeout(15, TimeUnit.SECONDS);
        httpClient.writeTimeout(15, TimeUnit.SECONDS);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        // add logging as last interceptor
        httpClient.addInterceptor(logging);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                // Customize the request
                Request request;
                if (accessToken != null) {
                    request = original.newBuilder()
                            .header("Accept", Constants.Http.CONTENT_TYPE_JSON)
                            .header(Constants.Http.HEADER_DEVICE_TOKEB, deviceToken)
                            .header(Constants.Http.HEADER_AUTH_TOKEN, accessToken)
                            .header("User-Agent", userAgentProvider.get())
                            .method(original.method(), original.body())
                            .build();
                } else {
                    request = original.newBuilder()
                            .header("User-Agent", userAgentProvider.get())
                            .method(original.method(), original.body())
                            .build();
                }
                Response response = chain.proceed(request);
                // Customize or return the response
                return response;
            }
        });

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = retrofitBuilder.client(client)
                .build();
        return retrofit.create(serviceClass);
    }

    public List<Unit> getUnits() {
        try {
            ApiResponse response = createAuthenticatedService(UnitService.class).getUnits().execute().body();
            if (null != response) {
                return response.getRecords();
            }
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return Collections.emptyList();
    }

    public List<Specialty> getSpecialities() {
        try {
            ApiResponse response = createAuthenticatedService(SpecialtyService.class).getSpecialty().execute().body();
            if (null != response) {
                return response.getRecords();
            }
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return Collections.emptyList();
    }

    public List<IncidentType> getIncidentTypes() {
        try {
            ApiResponse response = createAuthenticatedService(IncidentTypeService.class).getIncidentTypes().execute().body();
            if (null != response) {
                return response.getRecords();
            }
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return Collections.emptyList();
    }


    public ApiResponse<ApiAuthResponse> authenticate(String email, String password, String deviceID) {
        ApiRequest<User> request = new ApiRequest<>();
        AuthHeader authHeader = new AuthHeader();
        authHeader.setDeviceID(deviceID);
        authHeader.setAppCode(1);
        authHeader.setPlatform(1);
        request.setAuthHeader(authHeader);
        User user = new User();
        user.setUserName(email);
        user.setPassword(password);
        request.setRecord(user);
        try {
            return createService(UserService.class).authenticate(request).execute().body();
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return null;
    }

    public boolean doGCMRegistration(String platform, String key) {
        GCMRequest gcmRequest = new GCMRequest(platform, key);
        ApiRequest<GCMRequest> request = new ApiRequest<>();
        request.setRecord(gcmRequest);
        try {
            ApiResponse<String> response = createAuthenticatedService(GCMRegistrationService.class).register(request).execute().body();
            if (null != response && ApiResponse.Status.SUCCESS.equals(response.getStatus())) {
                return true;
            }
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return false;
    }

    public boolean doDeviceRegistration(String deviceToken, String key) {
        GCMRequest gcmRequest = new GCMRequest(deviceToken, key);
        ApiRequest<GCMRequest> request = new ApiRequest<>();
        request.setRecord(gcmRequest);
        try {
            ApiResponse<String> response = createAuthenticatedService(GCMRegistrationService.class).register(request).execute().body();
            if (null != response && ApiResponse.Status.SUCCESS.equals(response.getStatus())) {
                return true;
            }
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return false;
    }

    public ApiResponse<User> getProfile() {
        try {
            return createAuthenticatedService(UserService.class).getProfile().execute().body();
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return null;
    }

    public ApiResponse<String> getLatestAppVersion(String appName) {
        try {
            return createAuthenticatedService(GenericService.class).getLatestAppVersion(appName).execute().body();
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return null;
    }


    public com.synnefx.cqms.event.core.modal.event.incident.ApiResponse pushIncident(IncidentReport incidentReport) {
        ApiRequest<IncidentReport> request = new ApiRequest<>();
        request.setRecords(new ArrayList<IncidentReport>(1));
        request.getRecords().add(incidentReport);
        try {
            return createAuthenticatedService(IncidentReportService.class).pushRecords(request).execute().body();
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return null;
    }


    public com.synnefx.cqms.event.core.modal.event.medicationerror.ApiResponse pushMedicationError(MedicationError medicationError) {
        ApiRequest<MedicationError> request = new ApiRequest<>();
        request.setRecords(new ArrayList<MedicationError>(1));
        request.getRecords().add(medicationError);
        try {
            return createAuthenticatedService(MedicationErrorService.class).pushRecords(request).execute().body();
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return null;
    }

    public com.synnefx.cqms.event.core.modal.event.drugreaction.ApiResponse pushDrugReaction(AdverseDrugEvent adverseDrugEvent) {
        ApiRequest<AdverseDrugEvent> request = new ApiRequest<>();
        request.setRecords(new ArrayList<AdverseDrugEvent>(1));
        request.getRecords().add(adverseDrugEvent);
        try {
            return createAuthenticatedService(AdverseDrugReactionService.class).pushRecords(request).execute().body();
        } catch (Exception e) {
            bus.post(new NetworkErrorEvent(e));
        }
        return null;
    }
}