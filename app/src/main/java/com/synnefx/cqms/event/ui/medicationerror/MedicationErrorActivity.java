package com.synnefx.cqms.event.ui.medicationerror;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.BootstrapService;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.MainActivity;
import com.synnefx.cqms.event.ui.base.BootstrapFragmentActivity;
import com.synnefx.cqms.event.ui.incident.IncidentDetailsFragment;
import com.synnefx.cqms.event.ui.incident.IncidentPersonDetailsFragment;
import com.synnefx.cqms.event.ui.incident.ReportedByDetailsFragment;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SafeAsyncTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.synnefx.cqms.event.core.Constants.Extra.EDIT_REPORT_COMMAND;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_REF;

public class MedicationErrorActivity extends BootstrapFragmentActivity {


    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    EventBus eventBus;
    private MedicationError report;

    private Boolean doubleBackPressed = false;
    private Boolean editable = true;
    private Boolean viewable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        setContentView(R.layout.activity_incident_report);
        ButterKnife.bind(this);
        eventBus.register(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            report = (MedicationError) getIntent().getExtras().getSerializable(INCIDENT_ITEM);
            editable = getIntent().getExtras().getBoolean(EDIT_REPORT_COMMAND);
            viewable = getIntent().getBooleanExtra(getString(R.string.view_details),false);
            if(viewable){
                startActivity(new Intent(this,MedicationErrorViewActivity.class).putExtra(INCIDENT_ITEM,report));
                finish();
            }
            if (null == report) {
                Long reportRef = getIntent().getExtras().getLong(INCIDENT_REF);
                Log.e("reportRef ", String.valueOf(reportRef));
                if (null != reportRef && 0 < reportRef) {
                    report = new MedicationError();
                    report.setId(reportRef);
                    //report = databaseHelper.getMedicationErrorById(reportRef);
                }
            }
        }
        if (null == report) {
            report = new MedicationError();
        }
        Log.e("Opening  session", String.valueOf(report.getId()));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        loadFragment();
    }

    @Override
    protected void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    //Enable save draft feature to capture the current screen
    @Subscribe
    public void onEventListened(String data){
        if (data.equals(getString(R.string.save_btn_clicked))){
            //Toast.makeText(this, "Save button clicked", Toast.LENGTH_SHORT).show();
            doubleBackPressed = false;
        }
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
                hideProgress();
                Intent i = new Intent(getActivity(), MainActivity.class);
                i.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                getActivity().finish();
            }

            @Override
            protected void onFinally() throws RuntimeException {
                hideProgress();
                getActivity().finish();
            }

            @Override
            protected void onPreExecute() throws Exception {
                showProgressLogout();
            }
        }.execute();
    }


    @Override
    public void onBackPressed(){

        if(!doubleBackPressed && editable){
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
                    MedicationErrorActivity.super.onBackPressed();

                }
            });
            alertDialog.show();
        }else{
            //super.onBackPressed();
            navigateScreenBack();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    protected Activity getActivity() {
        return MedicationErrorActivity.this;
    }

    private void loadFragment() {
        MedicationErrorDetailsFragment detailsFragment = new MedicationErrorDetailsFragment();
        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            detailsFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, detailsFragment,"DetailsFragment")
                .commit();
    }

    private void navigateScreenBack(){
        //Find which fragment present at the container
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.incident_report_form_container);
        if (report.getId() != null &&  report.getId() >= 0) {
            report = databaseHelper.getMedicationErrorById(report.getId());
        }

        if (currentFragment instanceof MedicationErrorDetailsFragment){
            super.onBackPressed();
        }else if(currentFragment instanceof MedicationErrorPersonDetailsFragment){
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
                MedicationErrorDetailsFragment detailsFragment = new MedicationErrorDetailsFragment();
                detailsFragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.incident_report_form_container, detailsFragment,"DetailsFragment")
                        .commit();
                break;
            case 2:
                MedicationErrorPersonDetailsFragment personDetailsFragment = new MedicationErrorPersonDetailsFragment();
                personDetailsFragment.setArguments(bundle);
                FragmentManager fragmentManager1 = getSupportFragmentManager();
                fragmentManager1.beginTransaction()
                        .replace(R.id.incident_report_form_container, personDetailsFragment,"PersonDetailsFragment")
                        .commit();
                break;
            default:
                ErrorReportedByDetailsFragment reportedByDetailsFragment = new ErrorReportedByDetailsFragment();
                reportedByDetailsFragment.setArguments(bundle);
                FragmentManager fragmentManager2 = getSupportFragmentManager();
                fragmentManager2.beginTransaction()
                        .replace(R.id.incident_report_form_container, reportedByDetailsFragment,"FirstFragment")
                        .commit();

        }
    }


}
