package com.xose.cqms.event.ui.base;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.squareup.otto.Bus;
import com.xose.cqms.event.BootstrapApplication;
import com.xose.cqms.event.BootstrapServiceProvider;
import com.xose.cqms.event.R;
import com.xose.cqms.event.authenticator.LogoutService;
import com.xose.cqms.event.core.BootstrapService;
import com.xose.cqms.event.ui.MainActivity;
import com.xose.cqms.event.util.SafeAsyncTask;

import javax.inject.Inject;

import butterknife.ButterKnife;


/**
 * Base class for all Bootstrap Activities that need fragments.
 */
public abstract class BootstrapFragmentActivity extends AppCompatActivity {

    @Inject
    protected Bus eventBus;

    @Inject
    protected LogoutService logoutService;

    @Inject
    protected BootstrapServiceProvider serviceProvider;

    ConnectionDetector cd;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
    }

    @Override
    public void setContentView(final int layoutResId) {
        super.setContentView(layoutResId);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    /**
     * Function to display simple Alert Dialog
     *
     * @param context - application context
     * @param title   - alert dialog title
     * @param message - alert message
     */
    public void showAlertDialog(Context context, String title, String message,
                                int icon) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(icon);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    protected void showConnectionAlert() {
        showAlertDialog(this, "Connection Problem",
                "Not connected to internet. Check WiFi.",
                R.drawable.ic_action_network_wifi);
    }

    protected boolean isInternetAvaialable() {
        if (null == cd) {
            cd = new ConnectionDetector(getApplicationContext());
        }
        return cd.isConnectingToInternet();
    }

    protected abstract Activity getActivity();

    protected void logout() {
        logoutService.logout(new Runnable() {
            @Override
            public void run() {
                // Calling a refresh will force the service to look for a logged in user
                // and when it finds none the user will be requested to log in again.
                checkAuth();
            }
        });
    }

    private void checkAuth() {
        new SafeAsyncTask<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final BootstrapService svc = serviceProvider.getService(getActivity());
                return svc != null;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
                if (e instanceof OperationCanceledException) {
                    // cancelled the authentication process (back button, etc).
                    // Since auth could not take place, lets finish this activity.
                } else {
                    final Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            protected void onSuccess(final Boolean hasAuthenticated) throws Exception {
                super.onSuccess(hasAuthenticated);
            }
        }.execute();
    }
}
