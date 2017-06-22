package com.synnefx.cqms.event;

import android.accounts.AccountsException;
import android.app.Activity;

import com.synnefx.cqms.event.core.BootstrapService;

import java.io.IOException;

public interface BootstrapServiceProvider {

    BootstrapService getService(Activity activity) throws IOException, AccountsException;

    BootstrapService getAuthenticatedService(Activity activity) throws IOException, AccountsException;

    BootstrapService getAuthenticatedService() throws IOException, AccountsException;
}
