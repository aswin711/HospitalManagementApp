package com.synnefx.cqms.event.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.modal.IncidentType;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.base.BootstrapFragmentActivity;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SafeAsyncTask;

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

    private AlertDialog mAlert;
    private AlertDialog.Builder mAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        setContentView(R.layout.activity_import_config);

        if (!isInternetAvaialable()) {
            showConnectionAlert();
        } else {
                initImportAlert();
                importConfig();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        new SafeAsyncTask<Boolean>() {
            List<String> errorMessages = new ArrayList<String>(2);
            List<String> successessages = new ArrayList<String>(2);

            @Override
            public Boolean call() throws Exception {
                try {
                    String hospitalRef = PrefUtils.getFromPrefs(getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
                    if (null == hospitalRef || "".equals(hospitalRef.trim()) ) {
                        //Fetch Profile
                    }
                    if (null != hospitalRef && !"".equals(hospitalRef.trim())) {

                        List<Unit> units = serviceProvider.getAuthenticatedService(ImportConfigActivity.this).getUnits();

                        if (null != units && units.size() > 0) {
                            databaseHelper.insertOrUpdateUnits(units, hospitalRef);
                            successessages.add("Units added");
                        } else {
                            errorMessages.add("Units not configured");
                        }
                        List<IncidentType> incidentTypes = serviceProvider.getAuthenticatedService().getIncidentTypes();
                        if (null != incidentTypes && incidentTypes.size() > 0) {
                            databaseHelper.syncIncidentTypes(incidentTypes, hospitalRef);
                            successessages.add("Incident Types added");
                        } else {
                           errorMessages.add("Incident Types  not configured");
                        }
                    } else {
                        //Show warning
                    }
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
                    //Toaster.showLong(ImportConfigActivity.this, "Error : " + e.getMessage());
                    //Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            protected void onSuccess(final Boolean isUpdated) throws Exception {
                hideProgress();

                if (null != successessages) {
                    for (String msg : successessages) {
                        TextView displayView = (TextView) getLayoutInflater().inflate(R.layout.template_textview, null);
                        /*displayView.setText(msg);
                        statusView.addView(displayView);*/

                        showImportStatus("Configuration Imported Succesfully","Continue to Home Page?",1);
                        //showImportStatus("An Error Occured While Importing","Do you want to import config again?",2);
                    }
                }
                if (null != errorMessages) {
                    for (String msg : errorMessages) {
                        TextView displayView = (TextView) getLayoutInflater().inflate(R.layout.template_textview, null);
                        /*displayView.setText(msg);
                        statusView.addView(displayView);*/
                        showImportStatus("An Error Occured While Importing","Do you want to import config again?",2);
                    }
                }
            }

            @Override
            protected void onPreExecute() throws Exception {
                showProgressImport();
            }
        }.execute();

    }


    public void initImportAlert(){
        mAlertDialog = new AlertDialog.Builder(this);
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
    }

    public AlertDialog createAlert(){
        mAlert = mAlertDialog.create();
        return mAlert;
    }

    public void dismissAlert(){
        if (mAlert!=null){
            if (mAlert.isShowing()){
                mAlert.dismiss();
            }
        }

    }


    public void showImportStatus(String msg, String subMsg, final int status){
          dismissAlert();
        switch (status){
            case 1:
                mAlertDialog
                        .setMessage(subMsg)
                        .setTitle(msg)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
                createAlert().show();
                break;
            case 2:
                mAlertDialog
                        .setTitle(msg)
                        .setMessage(subMsg)
                        .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        startActivity(getIntent());
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                        moveTaskToBack(true);

                    }
                });
                createAlert().show();
                break;

            default:
                break;

        }

    }

    @Override
    protected void onDestroy() {
        dismissAlert();
        super.onDestroy();
    }
}
