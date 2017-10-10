package com.synnefx.cqms.event.ui.incident;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.BootstrapService;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.MainActivity;
import com.synnefx.cqms.event.ui.base.BootstrapFragmentActivity;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SafeAsyncTask;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.synnefx.cqms.event.core.Constants.Extra.EDIT_REPORT_COMMAND;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_REF;

public class IncidentReportActivity extends BootstrapFragmentActivity {


    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    EventBus eventBus;
    private IncidentReport report;
    private Boolean editable;

    private Boolean doubleBackPressed = false;


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
                    //report = databaseHelper.getIncidentReportById(reportRef);
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
    }



    @Override
    public void onBackPressed() {

        if(!doubleBackPressed){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setCancelable(true);
            alertDialog.setTitle("Save to Drafts");
            alertDialog.setMessage("Do you want to save it to drafts before exit?");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    doubleBackPressed = true;
                    eventBus.post(getString(R.string.save_draft));

                    onBackPressed();
                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doubleBackPressed = true;
                    onBackPressed();
                }
            });

            alertDialog.show();

        }else {
            super.onBackPressed();
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


}
