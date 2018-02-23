package com.synnefx.cqms.event.ui.incident;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.authenticator.BootstrapAuthenticatorActivity;
import com.synnefx.cqms.event.core.BootstrapService;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.MainActivity;
import com.synnefx.cqms.event.ui.base.BootstrapFragmentActivity;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SafeAsyncTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.synnefx.cqms.event.core.Constants.Extra.EDIT_REPORT_COMMAND;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_REF;

public class IncidentReportActivity extends BootstrapFragmentActivity implements IncidentDetailsFragment.DeleteReportListener {


    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    EventBus eventBus;
    private IncidentReport report;

    private Boolean editable = true;

    private Boolean doubleBackPressed = false;

    private Boolean delete = false;


    private AlertDialog deleteDialog;
    private AlertDialog saveDraftDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        setContentView(R.layout.activity_incident_report);
        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().getExtras() != null) {
            report = (IncidentReport) getIntent().getExtras().getSerializable(INCIDENT_ITEM);
            editable = getIntent().getBooleanExtra(EDIT_REPORT_COMMAND,false);

            if(!editable){
                startActivity(new Intent(this,IncidentReportViewActivity.class).putExtra(INCIDENT_ITEM,report));
                getActivity().finish();
            }
            if (null == report) {
                Long reportRef = getIntent().getExtras().getLong(INCIDENT_REF);
                Log.e("reportRef ", String.valueOf(reportRef));
                if (null != reportRef && 0 < reportRef) {
                    report = new IncidentReport();
                    report.setId(reportRef);
                }
            }
        }
        if (null == report) {
            report = new IncidentReport();
        }
        Log.e("Opening session", String.valueOf(report.getId()));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        loadFragment();
        createDeleteDilaog();
        createSaveDraftDialog();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void createSaveDraftDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Save to draft before exit?");
        alertDialog.setMessage("Do you want to save the details as a draft");
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventBus.post(getString(R.string.save_draft));
                dialog.dismiss();
                doubleBackPressed = true;
                onBackPressed();

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                IncidentReportActivity.super.onBackPressed();

            }
        });
        saveDraftDialog = alertDialog.create();

    }

    public void createDeleteDilaog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Unable to save draft due to missing mandatory fields");
        alertDialog.setMessage("Do you want to delete the report or continue editing?");
        alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteIncidentReportById(report.getId());
                dialog.dismiss();
                IncidentReportActivity.super.onBackPressed();

            }
        });
        alertDialog.setNegativeButton("CONTINUE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete = false;
                doubleBackPressed = false;
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        deleteDialog = alertDialog.create();
    }

    @Override
    public void onBackPressed(){

        if(!doubleBackPressed && !delete){
                if (!saveDraftDialog.isShowing()) {
                    saveDraftDialog.show();
                }
        }else if(delete){
            if (!deleteDialog.isShowing()) {
                deleteDialog.show();
            }
        }else{
            navigateScreenBack();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);
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
                PrefUtils.deleteFromPrefs();
                startActivity(new Intent(getApplicationContext(), BootstrapAuthenticatorActivity.class));
                finish();
            }
        });
    }


    protected Activity getActivity() {
        return IncidentReportActivity.this;
    }

    private void loadFragment() {
        IncidentDetailsFragment detailsFragment = new IncidentDetailsFragment();
        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            detailsFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, detailsFragment,"FirstFragment")
                .commit();
    }

    private void navigateScreenBack(){
        //Find which fragment present at the container
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.incident_report_form_container);
        if (report.getId() != null &&  report.getId() >= 0) {
            report = databaseHelper.getIncidentReportById(report.getId());
        }

        if (currentFragment instanceof IncidentDetailsFragment){
            super.onBackPressed();
        }else if(currentFragment instanceof IncidentPersonDetailsFragment){
            loadFragmentByTag(1);
        }else {
            loadFragmentByTag(2);
        }


    }

    private void loadFragmentByTag(int tagNo){
        Bundle bundle = new Bundle();
        if (null != report) {
            bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
        }
        switch (tagNo){
            case 1:
                IncidentDetailsFragment detailsFragment = new IncidentDetailsFragment();
                detailsFragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.incident_report_form_container, detailsFragment,"DetailsFragment")
                        .commit();
                break;
            case 2:
                IncidentPersonDetailsFragment personDetailsFragment = new IncidentPersonDetailsFragment();
                personDetailsFragment.setArguments(bundle);
                FragmentManager fragmentManager1 = getSupportFragmentManager();
                fragmentManager1.beginTransaction()
                        .replace(R.id.incident_report_form_container, personDetailsFragment,"PersonDetailsFragment")
                        .commit();
                break;
            default:
                ReportedByDetailsFragment reportedByDetailsFragment = new ReportedByDetailsFragment();
                reportedByDetailsFragment.setArguments(bundle);
                FragmentManager fragmentManager2 = getSupportFragmentManager();
                fragmentManager2.beginTransaction()
                        .replace(R.id.incident_report_form_container, reportedByDetailsFragment,"FirstFragment")
                        .commit();

        }
    }


    @Override
    public void deleteReport() {
        delete = true;
        onBackPressed();
    }
}
