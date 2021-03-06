package com.synnefx.cqms.event.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
import com.synnefx.cqms.event.util.NotificationUtils;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SafeAsyncTask;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;

import static com.synnefx.cqms.event.core.Constants.Notification.IMPORT_INCIDENTTYPE_NOTIFICATION_ID;
import static com.synnefx.cqms.event.core.Constants.Notification.IMPORT_NOTIFICATION_ID;
import static com.synnefx.cqms.event.core.Constants.Notification.IMPORT_UNIT_NOTIFICATION_ID;

public class ImportConfigActivity extends BootstrapFragmentActivity {

    @Inject
    Bus eventBus;


    @Inject
    protected BootstrapServiceProvider serviceProvider;

    @Inject
    DatabaseHelper databaseHelper;
    @Inject
    protected NotificationManager notificationManager;

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
                importConfigServices();
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


    private void importConfigServices(){
        new ImportServices().execute();
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

    @Override
    public void onBackPressed() {
        hideProgress();
        finish();
    }

    //Importing Units and Types
    private class ImportServices extends AsyncTask<Void,Integer,Void>{

        List<String> errorMessages = new ArrayList<String>(2);
        List<String> successessages = new ArrayList<String>(2);

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String hospitalRef = PrefUtils.getFromPrefs(getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
                if (null == hospitalRef || "".equals(hospitalRef.trim()) ) {
                    //Fetch Profile
                }
                if (null != hospitalRef && !"".equals(hospitalRef.trim())) {
                    updateNotification("Configuration import in progress");
                    publishProgress(0);
                    List<Unit> units = serviceProvider.getAuthenticatedService(ImportConfigActivity.this).getUnits();

                    if (null != units && units.size() > 0) {
                        databaseHelper.insertOrUpdateUnits(units, hospitalRef);
                        successessages.add("Units added");
                        publishProgress(1);

                    } else {
                        updateClosableNotification("Units not configured", IMPORT_UNIT_NOTIFICATION_ID);
                        errorMessages.add("Units not configured");
                    }

                    publishProgress(2);
                    List<IncidentType> incidentTypes = serviceProvider.getAuthenticatedService().getIncidentTypes();
                    if (null != incidentTypes && incidentTypes.size() > 0) {
                        databaseHelper.syncIncidentTypes(incidentTypes, hospitalRef);
                        successessages.add("Incident Types added");
                        publishProgress(3);

                    } else {
                        updateClosableNotification("Incident Types  not configured", IMPORT_INCIDENTTYPE_NOTIFICATION_ID);
                        errorMessages.add("Incident Types  not configured");
                    }
                } else {
                    //Show warning
                }
            } catch (Exception e) {
                errorMessages.add("Error while importing configuration from server" + e.getMessage());
                Log.e("Error", "Import", e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            showProgressImport();
            updateProgressImport("Units",0);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideProgress();

            if (successessages.size()>0) {
                updateClosableNotification("Configuration update completed", IMPORT_NOTIFICATION_ID);
                showImportStatus("Configuration Imported Succesfully","Continue to Home Page?",1);
            }
            if (errorMessages.size()>0) {
                updateClosableNotification("Configuration update completed, Some units are not configured", IMPORT_NOTIFICATION_ID);
                showImportStatus("An Error Occurred While Importing",errorMessages.get(0)+". Do you want to import config again?",2);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (values[0]){
                case 0:
                    updateProgressImport("Units",0);
                    break;
                case 1:
                    updateProgressImport("Units",1);
                    break;
                case 2:
                    updateProgressImport("Types",1);
                    break;
                case 3:
                    updateProgressImport("Types",2);
                    break;
                default:
                    break;
            }
        }
    }


    private void updateNotification(String message) {
        if (null != notificationManager) {
            notificationManager.notify(IMPORT_NOTIFICATION_ID, NotificationUtils.getNotification(getApplicationContext(), getString(R.string.app_full_name)+": Configuration update", message));
        }
    }

    private void updateClosableNotification(String message, int notificationID) {
        if (null != notificationManager) {
            final Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
            NotificationCompat.Builder builder = NotificationUtils.getNotificationBuilder(getApplicationContext(), getString(R.string.app_full_name)+": Configuration update", message, pendingIntent)
                    .setContentText(message);
            builder.setOngoing(false);
            notificationManager.notify(notificationID, builder.build());
        }
    }
}
