package com.synnefx.cqms.event.ui.base;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.authenticator.BootstrapAuthenticatorActivity;
import com.synnefx.cqms.event.authenticator.LogoutService;
import com.synnefx.cqms.event.core.BootstrapService;
import com.synnefx.cqms.event.events.NetworkErrorEvent;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.sync.conf.ConfSyncContentProvider;
import com.synnefx.cqms.event.sync.drugreaction.DrugReactionSyncContentProvider;
import com.synnefx.cqms.event.sync.incident.IncidentReportSyncContentProvider;
import com.synnefx.cqms.event.sync.medicationerror.MedicationErrorSyncContentProvider;
import com.synnefx.cqms.event.ui.MainActivity;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SafeAsyncTask;
import com.synnefx.cqms.event.util.Toaster;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

/**
 * Base activity for a Bootstrap activity which does not use fragments.
 */
public abstract class BootstrapActivity extends AppCompatActivity {

    @Inject
    protected Bus bus;

    private ConnectionDetector cd;

    @Inject
    protected LogoutService logoutService;

    @Inject
    protected BootstrapServiceProvider serviceProvider;

    @Inject
    protected DatabaseHelper databaseHelper;

    protected ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BootstrapApplication.component().inject(this);
        mProgressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void setContentView(final int layoutResId) {
        super.setContentView(layoutResId);

        // Used to inject views with the Butterknife library
        ButterKnife.bind(this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            // This is the home button in the top left corner of the screen.
            case android.R.id.home:
                // Don't call finish! Because activity could have been started by an
                // outside activity and the home button would not operated as expected!
                final Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                return true;
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        // Setting Dialog Title
        alertDialog.setTitle(title);
        // Setting Dialog Message
        alertDialog.setMessage(message);
        // Setting alert dialog icon
        alertDialog.setIcon(icon);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        // Showing Alert Message
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

    @Subscribe
    public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
        // Could not authorize for some reason.
        Toaster.showLong(this, R.string.message_bad_connection);
    }


    protected abstract Activity getActivity();

    protected void logout() {
        logoutService.logout(new Runnable() {
            @Override
            public void run() {
                // Calling a refresh will force the service to look for a logged in user
                // and when it finds none the user will be requested to log in again.
                //checkAuth();
                PrefUtils.deleteFromPrefs();
                startActivity(new Intent(getApplicationContext(), BootstrapAuthenticatorActivity.class));
                finish();
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
                }
            }

            @Override
            protected void onSuccess(final Boolean hasAuthenticated) throws Exception {
                //Toast.makeText(context, "Logout.", Toast.LENGTH_SHORT).show();
               // Log.d("LOGOUT","Logged out");
                //super.onSuccess(hasAuthenticated);
            }

            @Override
            protected void onFinally() throws RuntimeException {
                Log.d("LOGOUT","Logged out");
                hideProgress();
            }

            @Override
            protected void onPreExecute() throws Exception {
                showProgressLogout();
            }
        }.execute();
    }


    private void showProgressLogout(){
        mProgressDialog.setMessage("Logging out..");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        hideProgress();
        mProgressDialog.show();
    }

    private void hideProgress(){
        if (mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }
}
