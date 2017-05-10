package com.xose.cqms.event.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;

import com.xose.cqms.event.core.Constants;
import com.xose.cqms.event.util.SafeAsyncTask;

import java.io.IOException;

import timber.log.Timber;

import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static com.xose.cqms.event.core.Constants.Auth.AUTHTOKEN_TYPE;
import static com.xose.cqms.event.core.Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE;

/**
 * Bridge class that obtains a API key for the currently configured account
 */
public class ApiKeyProvider {

    private AccountManager accountManager;

    private Context context;

    protected String token;

    public ApiKeyProvider(AccountManager accountManager, Context context) {
        this.accountManager = accountManager;
        this.context = context;
    }

    /**
     * This call blocks, so shouldn't be called on the UI thread.
     * This call is what makes the login screen pop up. If the user has
     * not logged in there will no accounts in the {@link android.accounts.AccountManager}
     * and therefore the Activity that is referenced in the
     * {@link com.xose.cqms.event.authenticator.BootstrapAccountAuthenticator} will get started.
     * If you want to remove the authentication then you can comment out the code below and return a string such as
     * "foo" and the authentication process will not be kicked off. Alternatively, you can remove this class
     * completely and clean up any references to the authenticator.
     *
     * @return API key to be used for authorization with a
     * {@link com.xose.cqms.event.core.BootstrapService} instance
     * @throws AccountsException
     * @throws IOException
     */
    public String getAuthKey(final Activity activity) throws AccountsException, IOException {
        final AccountManagerFuture<Bundle> accountManagerFuture
                = accountManager.getAuthTokenByFeatures(BOOTSTRAP_ACCOUNT_TYPE,
                AUTHTOKEN_TYPE, new String[0], activity, null, null, null, null);
        return accountManagerFuture.getResult().getString(KEY_AUTHTOKEN);
    }

    public String getDeviceID() {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public String getAuthKey() throws AccountsException, IOException {
        //new AccountAccessTask(context).execute();
        //TOTO wont work.
        //return "" ;
        if (accountManager != null) {
            final Account[] accounts = accountManager
                    .getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
            if (accounts.length > 0) {
                final AccountManagerFuture<Bundle> accountManagerFuture = accountManager.getAuthToken(accounts[0], AUTHTOKEN_TYPE, null, true, null, null);
                return accountManagerFuture.getResult().getString(KEY_AUTHTOKEN);
            }
        }
        return "";
    }

    public String getAuthKey(Account account) throws AccountsException, IOException {
        //new AccountAccessTask(context).execute();
        //TOTO wont work.
        //return "" ;
        if (accountManager != null && null != account) {
            final AccountManagerFuture<Bundle> accountManagerFuture = accountManager.getAuthToken(account, AUTHTOKEN_TYPE, null, true, null, null);
            return accountManagerFuture.getResult().getString(KEY_AUTHTOKEN);
        }
        return "";
    }

    private class AccountAccessTask extends SafeAsyncTask<String> {

        private final Context taskContext;

        protected AccountAccessTask(final Context context) {
            this.taskContext = context;
        }

        @Override
        public String call() throws Exception {

            final AccountManager accountManagerWithContext = AccountManager.get(taskContext);
            if (accountManagerWithContext != null) {
                final Account[] accounts = accountManagerWithContext
                        .getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
                if (accounts.length > 0) {
                    final AccountManagerFuture<Bundle> accountManagerFuture = accountManagerWithContext.getAuthToken(accounts[0], AUTHTOKEN_TYPE, null, true, null, null);
                    return accountManagerFuture.getResult().getString(KEY_AUTHTOKEN);
                }
            } else {
                Timber.w("accountManagerWithContext is null");
            }

            return null;
        }

        @Override
        protected void onSuccess(final String authKey) throws Exception {
            super.onSuccess(authKey);
            token = authKey;
        }

        @Override
        protected void onException(final Exception e) throws RuntimeException {
            super.onException(e);
            Timber.e(e.getCause(), "Logout failed.");
        }
    }
}
