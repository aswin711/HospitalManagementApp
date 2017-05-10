package com.xose.cqms.event;

import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.squareup.otto.Bus;
import com.xose.cqms.event.authenticator.ApiKeyProvider;
import com.xose.cqms.event.authenticator.LogoutService;
import com.xose.cqms.event.authenticator.LogoutServiceImpl;
import com.xose.cqms.event.core.BootstrapService;
import com.xose.cqms.event.core.Constants;
import com.xose.cqms.event.core.PostFromAnyThreadBus;
import com.xose.cqms.event.core.UserAgentProvider;
import com.xose.cqms.event.sqlite.AppDao;
import com.xose.cqms.event.sqlite.DatabaseHelper;
import com.xose.cqms.event.sync.conf.ConfSyncAdapter;
import com.xose.cqms.event.sync.drugreaction.DrugReactionSyncAdapter;
import com.xose.cqms.event.sync.drugreaction.DrugReactionSyncLocalDatastore;
import com.xose.cqms.event.sync.drugreaction.DrugReactionSyncRemoteDatastore;
import com.xose.cqms.event.sync.incident.IncidentReportSyncAdapter;
import com.xose.cqms.event.sync.incident.IncidentReportSyncLocalDatastore;
import com.xose.cqms.event.sync.incident.IncidentReportSyncRemoteDatastore;
import com.xose.cqms.event.sync.medicationerror.MedicationErrorSyncAdapter;
import com.xose.cqms.event.sync.medicationerror.MedicationErrorSyncLocalDatastore;
import com.xose.cqms.event.sync.medicationerror.MedicationErrorSyncRemoteDatastore;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module
public class BootstrapModule {

    @Singleton
    @Provides
    Bus provideOttoBus() {
        return new PostFromAnyThreadBus();
    }

    @Provides
    @Singleton
    LogoutService provideLogoutService(final Context context, final AccountManager accountManager, final BootstrapServiceProvider serviceProvider) {
        return new LogoutServiceImpl(context, accountManager, serviceProvider);
    }


    @Provides
    BootstrapService provideBootstrapService(Retrofit.Builder retrofitBuilder, UserAgentProvider userAgentProvider, ApiKeyProvider apiKeyProvider, Bus bus) {
        return new BootstrapService(retrofitBuilder, userAgentProvider, apiKeyProvider, bus);
    }

    @Provides
    BootstrapServiceProvider provideBootstrapServiceProvider(Retrofit.Builder retrofitBuilder, UserAgentProvider userAgentProvider, ApiKeyProvider apiKeyProvider, Bus bus) {
        return new BootstrapServiceProviderImpl(retrofitBuilder, userAgentProvider, apiKeyProvider, bus);
    }

    @Provides
    ApiKeyProvider provideApiKeyProvider(AccountManager accountManager, final Context context) {
        return new ApiKeyProvider(accountManager, context);
    }


    //@Provides
    //Gson provideGson() {

    /**
     * GSON instance to use for all request  with date format set up for proper parsing.
     * <p/>
     * You can also configure GSON with different naming policies for your API.
     * Maybe your API is Rails API and all json values are lower case with an underscore,
     * like this "first_name" instead of "firstName".
     * You can configure GSON as such below.
     * <p/>
     * <p/>
     * public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd")
     * .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();
     */

    //    return new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    // }
    @Provides
    Gson provideGson() {
        JsonDeserializer<GregorianCalendar> deser = new JsonDeserializer<GregorianCalendar>() {
            @Override
            public GregorianCalendar deserialize(JsonElement json, Type arg1,
                                                 JsonDeserializationContext arg2)
                    throws JsonParseException {
                GregorianCalendar cal = new GregorianCalendar();
                if (json == null)
                    return null;
                else {
                    cal.setTimeInMillis(json.getAsLong());
                    return cal;
                }
            }
        };

        JsonDeserializer<Boolean> boolDeser = new JsonDeserializer<Boolean>() {
            @Override
            public Boolean deserialize(JsonElement json, Type arg1,
                                       JsonDeserializationContext arg2)
                    throws JsonParseException {
                if (json == null) {
                    return Boolean.FALSE;
                } else if ("true".equalsIgnoreCase(json.getAsJsonPrimitive().getAsString())) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            }
        };

        JsonSerializer<GregorianCalendar> ser = new JsonSerializer<GregorianCalendar>() {

            @Override
            public JsonElement serialize(GregorianCalendar cal, Type arg1,
                                         JsonSerializationContext arg2) {
                Log.e("debug", cal.getTimeZone().toString());
                return cal == null ? null : new JsonPrimitive(
                        cal.getTimeInMillis());
            }
        };
        // 3. build jsonObject
        return new GsonBuilder().registerTypeAdapter(GregorianCalendar.class,
                ser).registerTypeAdapter(
                Calendar.class, deser).registerTypeAdapter(Boolean.class, boolDeser).excludeFieldsWithoutExposeAnnotation().create();

    }

    //@Provides
    //RestErrorHandler provideRestErrorHandler(Bus bus) {
    //   return new RestErrorHandler(bus);
    // }

    @Provides
    UserAgentProvider providesUserAgentProvider(ApplicationInfo appInfo, PackageInfo packageInfo, TelephonyManager telephonyManager, ClassLoader classLoader) {
        return new UserAgentProvider(appInfo, packageInfo, telephonyManager, classLoader);
    }


    /*
    @Provides
    RestAdapterRequestInterceptor provideRestAdapterRequestInterceptor(UserAgentProvider userAgentProvider) {
        return new RestAdapterRequestInterceptor(userAgentProvider);
    }

    @Provides @Singleton
    RestAdapter.Builder provideRestAdapterBuilder(RestErrorHandler restErrorHandler, RestAdapterRequestInterceptor restRequestInterceptor, Gson gson) {
        return new RestAdapter.Builder()
                .setEndpoint(Constants.Http.URL_BASE)
                .setErrorHandler(restErrorHandler)
                .setRequestInterceptor(restRequestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson));
    }

    @Provides @Singleton
    RestAdapter provideRestAdapter(RestErrorHandler restErrorHandler, RestAdapterRequestInterceptor restRequestInterceptor, Gson gson) {
        return new RestAdapter.Builder()
                .setEndpoint(Constants.Http.URL_BASE)
                .setErrorHandler(restErrorHandler)
                .setRequestInterceptor(restRequestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    */

    @Provides
    Retrofit.Builder provideRetrofitBuilder(Gson gson) {
        return new Retrofit.Builder().baseUrl(Constants.Http.URL_BASE).addConverterFactory(GsonConverterFactory.create(gson));
    }


    @Provides
    @Singleton
    DatabaseHelper provideDatabaseHelper(Context appContext) {
        return new DatabaseHelper(appContext);
    }

    @Provides
    @Singleton
    AppDao provideAppDao(Context appContext) {
        return new AppDao(appContext);
    }


    @Provides
    ConfSyncAdapter provideConfSyncAdapter(Context context, NotificationManager notificationManager, BootstrapServiceProvider serviceProvider, DatabaseHelper databaseHelper) {
        return new ConfSyncAdapter(context, notificationManager, serviceProvider, databaseHelper);
    }

    @Provides
    @Singleton
    IncidentReportSyncRemoteDatastore provideIncidentReportSyncRemoteDatastore(BootstrapServiceProvider bootstrapServiceProvider) {
        return new IncidentReportSyncRemoteDatastore(bootstrapServiceProvider);
    }

    @Provides
    @Singleton
    IncidentReportSyncLocalDatastore provideIncidentReportSyncLocalDatastore(AppDao dao) {
        return new IncidentReportSyncLocalDatastore(dao);
    }

    @Provides
    IncidentReportSyncAdapter provideIncidentReportSyncAdapter(Context context, NotificationManager notificationManager, IncidentReportSyncLocalDatastore localDatastore, IncidentReportSyncRemoteDatastore remoteDatastore) {
        return new IncidentReportSyncAdapter(context, notificationManager, localDatastore, remoteDatastore);
    }


    @Provides
    @Singleton
    MedicationErrorSyncRemoteDatastore provideMedicationErrorSyncRemoteDatastore(BootstrapServiceProvider bootstrapServiceProvider) {
        return new MedicationErrorSyncRemoteDatastore(bootstrapServiceProvider);
    }

    @Provides
    @Singleton
    MedicationErrorSyncLocalDatastore provideMedicationErrorSyncLocalDatastore(AppDao dao) {
        return new MedicationErrorSyncLocalDatastore(dao);
    }

    @Provides
    MedicationErrorSyncAdapter provideMedicationErrorSyncAdapter(Context context, NotificationManager notificationManager, MedicationErrorSyncLocalDatastore localDatastore, MedicationErrorSyncRemoteDatastore remoteDatastore) {
        return new MedicationErrorSyncAdapter(context, notificationManager, localDatastore, remoteDatastore);
    }


    @Provides
    @Singleton
    DrugReactionSyncRemoteDatastore provideDrugReactionSyncRemoteDatastore(BootstrapServiceProvider bootstrapServiceProvider) {
        return new DrugReactionSyncRemoteDatastore(bootstrapServiceProvider);
    }

    @Provides
    @Singleton
    DrugReactionSyncLocalDatastore provideDrugReactionSyncLocalDatastore(AppDao dao) {
        return new DrugReactionSyncLocalDatastore(dao);
    }

    @Provides
    DrugReactionSyncAdapter provideDrugReactionSyncAdapter(Context context, NotificationManager notificationManager, DrugReactionSyncLocalDatastore localDatastore, DrugReactionSyncRemoteDatastore remoteDatastore) {
        return new DrugReactionSyncAdapter(context, notificationManager, localDatastore, remoteDatastore);
    }

}
