package com.xose.cqms.event.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.xose.cqms.event.BootstrapApplication;
import com.xose.cqms.event.BootstrapServiceProvider;
import com.xose.cqms.event.R;
import com.xose.cqms.event.core.modal.Unit;
import com.xose.cqms.event.sqlite.DatabaseHelper;
import com.xose.cqms.event.ui.base.BootstrapFragmentActivity;
import com.xose.cqms.event.util.PrefUtils;
import com.xose.cqms.event.util.SafeAsyncTask;
import com.xose.cqms.event.util.Toaster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;

public class ImportConfigActivity extends BootstrapFragmentActivity {

    @Inject
    Bus eventBus;


    @Inject
    protected BootstrapServiceProvider serviceProvider;

    @Inject
    DatabaseHelper databaseHelper;

    @Bind(R.id.status_message_view)
    protected LinearLayout statusView;


    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        setContentView(R.layout.activity_import_config);
        if (!isInternetAvaialable()) {
            showConnectionAlert();
        } else {
            //Fabric.with(this, new Crashlytics());
            importConfig();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_import_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected Activity getActivity() {
        return ImportConfigActivity.this;
    }

    private void importConfig() {

        progress = new ProgressDialog(this);
        progress.setMessage("Importing Services");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setProgress(10);
        progress.show();
        new SafeAsyncTask<Boolean>() {
            List<String> errorMessages = new ArrayList<String>(2);
            List<String> successessages = new ArrayList<String>(2);

            @Override
            public Boolean call() throws Exception {
                try {
                    Long hospitalRef = PrefUtils.getLongFromPrefs(getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
                    if (null == hospitalRef || 0 >= hospitalRef) {
                        //Fetch Profile
                    }
                    if (null != hospitalRef && 0 < hospitalRef) {
                        setProgressPercent(30);
                        List<Unit> units = serviceProvider.getAuthenticatedService(ImportConfigActivity.this).getUnits();
                        setProgressPercent(60);
                        if (null != units && units.size() > 0) {
                            databaseHelper.insertOrUpdateUnits(units, hospitalRef);
                            successessages.add("Units added");
                        } else {
                            errorMessages.add("Units not configured");
                        }
                    } else {
                        //Show warning
                    }
                    setProgressPercent(80);
                    return true;
                } catch (Exception e) {
                    errorMessages.add("Error while importing configuration from server" + e.getMessage());
                    Log.e("Error", "Import", e);
                }
                return false;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
                if (e instanceof OperationCanceledException) {
                    // User cancelled the authentication process (back button, etc).
                    // Since auth could not take place, lets finish this activity.
                    finish();
                } else {
                    Toaster.showLong(ImportConfigActivity.this, "Error : " + e.getMessage());
                }
                progress.hide();
            }

            @Override
            protected void onSuccess(final Boolean isUpdated) throws Exception {
                super.onSuccess(isUpdated);
                // userHasAuthenticated = true;
                // SnackBar snackBar = SnackBar.make(getContext()).
                //Toaster.showLong(ImportConfigActivity.this, messages.);
                if (null != successessages) {
                    for (String msg : successessages) {
                        TextView displayView = (TextView) getLayoutInflater().inflate(R.layout.template_textview, null);
                        displayView.setText(msg);
                        statusView.addView(displayView);
                    }
                }
                if (null != errorMessages) {
                    for (String msg : errorMessages) {
                        TextView displayView = (TextView) getLayoutInflater().inflate(R.layout.template_textview, null);
                        displayView.setText(msg);
                        statusView.addView(displayView);
                    }
                }
                progress.hide();
            }

            protected void onProgressUpdate(Integer... progress) {
                setProgressPercent(progress[0]);
            }
        }.execute();

    }

    private void setProgressPercent(int perc) {
        if (null != progress) {
            progress.setProgress(perc);
        }
    }

}
