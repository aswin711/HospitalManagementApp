package com.synnefx.cqms.event;

import android.accounts.AccountsException;
import android.app.Activity;
import android.provider.Settings;
import android.util.Log;

import com.squareup.otto.Bus;
import com.synnefx.cqms.event.authenticator.ApiKeyProvider;
import com.synnefx.cqms.event.core.BootstrapService;
import com.synnefx.cqms.event.core.UserAgentProvider;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.util.PrefUtils;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Retrofit;


/**
 * Provider for a {@link com.synnefx.cqms.event.core.BootstrapService} instance
 */
public class BootstrapServiceProviderImpl implements BootstrapServiceProvider {

    //private RestAdapter.Builder restAdapterBuilder;
    private Retrofit.Builder retrofitBuilder;
    private Bus bus;
    private UserAgentProvider userAgentProvider;
    private ApiKeyProvider keyProvider;



    public BootstrapServiceProviderImpl(Retrofit.Builder retrofitBuilder, UserAgentProvider userAgentProvider, ApiKeyProvider keyProvider, Bus bus) {
        this.retrofitBuilder = retrofitBuilder;
        this.keyProvider = keyProvider;
        this.userAgentProvider = userAgentProvider;
        this.bus = bus;
    }

    /**
     * Get service for configured key provider
     * <p/>
     * This method gets an auth key and so it blocks and shouldn't be called on the main thread.
     *
     * @return bootstrap service
     * @throws IOException
     * @throws AccountsException
     */
    @Override
    public BootstrapService getService(final Activity activity)
            throws IOException, AccountsException {
        // The call to keyProvider.getAuthKey(...) is what initiates the login screen. Call that now.
        keyProvider.getAuthKey(activity);
        return new BootstrapService(retrofitBuilder, userAgentProvider, keyProvider, bus);
    }

    @Override
    public BootstrapService getAuthenticatedService(final Activity activity)
            throws IOException, AccountsException {

        // The call to keyProvider.getAuthKey(...) is what initiates the login screen. Call that now.
        String key = keyProvider.getAuthKey(activity);
        //String android_id = Settings.Secure.getString(activity.getContentResolver(),
        //        Settings.Secure.ANDROID_ID);
        String deviceToken = PrefUtils.getDeviceToken();
        BootstrapService service = new BootstrapService(retrofitBuilder, userAgentProvider, key, deviceToken, bus);
        return service;
    }


    @Override
    public BootstrapService getAuthenticatedService()
            throws IOException, AccountsException {
        // The call to keyProvider.getAuthKey(...) is what initiates the login screen. Call that now.
        ///String android_id = keyProvider.getDeviceID();
        String deviceToken = PrefUtils.getDeviceToken();
        String key = keyProvider.getAuthKey();
        Log.e("Login",key+"\n"+deviceToken);
        BootstrapService service = new BootstrapService(retrofitBuilder, userAgentProvider, key, deviceToken, bus);
        return service;
    }
}
