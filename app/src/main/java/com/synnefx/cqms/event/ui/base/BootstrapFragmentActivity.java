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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.squareup.otto.Bus;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.authenticator.LogoutService;
import com.synnefx.cqms.event.core.BootstrapService;
import com.synnefx.cqms.event.ui.MainActivity;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SafeAsyncTask;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;


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

    protected ProgressDialog mProgressDialog;

    ConnectionDetector cd;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        mProgressDialog = new ProgressDialog(this);

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


    protected void showProgressLogout(){
        mProgressDialog.setMessage("Logging out....");
        mProgressDialog.setIndeterminate(true);
        hideProgress();
        mProgressDialog.show();
    }

    protected void showProgressImport(){
        mProgressDialog.setMessage("Importing Services....");
        mProgressDialog.setIndeterminate(true);
        hideProgress();
        mProgressDialog.show();
    }

    protected void updateProgressImport(String update,int count){
        if (mProgressDialog.isShowing()){
            mProgressDialog.setMessage("Importing "+update+"....    ("+count+"/2)");

        }
    }

    protected void hideProgress(){
        if (mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            // This is the home button in the top left corner of the screen.
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void logout() {
        logoutService.logout(new Runnable() {
            @Override
            public void run() {
                // Calling a refresh will force the service to look for a logged in user
                // and when it finds none the user will be requested to log in again.
                PrefUtils.deleteFromPrefs(getApplicationContext());
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
                }
            }

            @Override
            protected void onSuccess(final Boolean hasAuthenticated) throws Exception {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                super.onSuccess(hasAuthenticated);
            }
        }.execute();
    }
}
