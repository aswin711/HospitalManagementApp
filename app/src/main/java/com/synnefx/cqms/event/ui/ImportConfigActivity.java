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

import static com.synnefx.cqms.event.core.Constants.Notification.IMPORT_INCIDENTTYPE_NOTIFICATION_ID;

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
            if(PrefUtils.isUserLoggedIn()){
                importConfig();
            }else {
                   finish();
            }
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

        //Toast.makeText(this, "Importing", Toast.LENGTH_SHORT).show();
        progress = new ProgressDialog(this);
        progress.setMessage("Importing Services");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        progress.setProgress(10);
        progress.show();
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
                        setProgressPercent(30);
                        List<Unit> units = serviceProvider.getAuthenticatedService(ImportConfigActivity.this).getUnits();
                        setProgressPercent(60);
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
                    //Toaster.showLong(ImportConfigActivity.this, "Error : " + e.getMessage());
                    //Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                progress.hide();
            }

            @Override
            protected void onSuccess(final Boolean isUpdated) throws Exception {
                super.onSuccess(isUpdated);
                // userHasAuthenticated = true;
                // SnackBar snackBar = SnackBar.make(getContext()).
                //Toaster.showLong(ImportConfigActivity.this, messages.);
                progress.hide();
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

    public void showImportStatus(String msg, String subMsg, final int status){

        switch (status){
            case 1:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                int imageResource = android.R.drawable.ic_dialog_alert;
                alertDialog.setTitle(msg);
                alertDialog.setMessage(subMsg);
                alertDialog.setIcon(imageResource);

                alertDialog.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        //finish();
                            //finish();
                    }
                });

                alertDialog.show();
                break;
            case 2:
                AlertDialog.Builder alertDialog1 = new AlertDialog.Builder(getActivity());
                int imageResource1 = android.R.drawable.ic_dialog_alert;
                alertDialog1.setTitle(msg);
                alertDialog1.setMessage(subMsg);
                alertDialog1.setIcon(imageResource1);

                alertDialog1.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        startActivity(getIntent());
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        moveTaskToBack(true);

                    }
                });
                alertDialog1.show();
                break;

            default:
                break;

        }

    }
}
