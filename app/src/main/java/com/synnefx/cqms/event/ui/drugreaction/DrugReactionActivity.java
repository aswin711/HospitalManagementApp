package com.synnefx.cqms.event.ui.drugreaction;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.authenticator.BootstrapAuthenticatorActivity;
import com.synnefx.cqms.event.core.BootstrapService;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.core.modal.event.drugreaction.DrugInfo;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.MainActivity;
import com.synnefx.cqms.event.ui.base.BootstrapFragmentActivity;
import com.synnefx.cqms.event.ui.incident.IncidentReportViewActivity;
import com.synnefx.cqms.event.ui.medicationerror.ErrorReportedByDetailsFragment;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorDetailsFragment;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorPersonDetailsFragment;
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

public class DrugReactionActivity extends BootstrapFragmentActivity {


    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    EventBus eventBus;
    private AdverseDrugEvent report;

    private Boolean editable;
    private boolean doubleBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        setContentView(R.layout.activity_incident_report);
        ButterKnife.bind(this);
        eventBus.register(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            report = (AdverseDrugEvent) getIntent().getExtras().getSerializable(INCIDENT_ITEM);
            editable = getIntent().getBooleanExtra(EDIT_REPORT_COMMAND,false);
            if(!editable){
                startActivity(new Intent(this,DrugReactionReportViewActivity.class).putExtra(INCIDENT_ITEM,report));
                getActivity().finish();
            }
            Long reportRef = 0l;
            if (null == report) {
                reportRef = getIntent().getExtras().getLong(INCIDENT_REF);
                Log.e("reportRef ", String.valueOf(reportRef));
                if (null != reportRef && 0 < reportRef) {
                    report = new AdverseDrugEvent();
                    report.setPersonInvolved(new PersonInvolved());
                    report.setId(reportRef);
                }
            }
        }
        if (null == report) {
            report = new AdverseDrugEvent();
            report.setPersonInvolved(new PersonInvolved());
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
            doubleBackPressed = false;
        }
    }

    protected Activity getActivity() {
        return DrugReactionActivity.this;
    }


    @Override
    public void onBackPressed() {
        hideSoftKeyboard(getActivity());
        if (!doubleBackPressed) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setCancelable(true);
            alertDialog.setTitle("Save to Drafts");
            alertDialog.setMessage("Do you want to save it to drafts before exit?");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    doubleBackPressed = true;
                    eventBus.post(getString(R.string.save_draft));
                    onBackPressed();
                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    DrugReactionActivity.super.onBackPressed();
                }
            });

            alertDialog.show();

        } else {
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
    private void loadFragment() {
        PatientDetailsFragment detailsFragment = new PatientDetailsFragment();
        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            bundle.putSerializable(Constants.Extra.PATIENT_REF, report.getPersonInvolvedRef());
            detailsFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, detailsFragment)
                .commit();
    }

    private void navigateScreenBack(){
        //Find which fragment present at the container
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.incident_report_form_container);
        if (report.getId() != null &&  report.getId() >= 0) {
            report = databaseHelper.getAdverseDrugEventById(report.getId());
        }

        if (currentFragment instanceof PatientDetailsFragment){
            super.onBackPressed();
        }else if(currentFragment instanceof DrugReactionDiagnosisDetailsFragment){
            loadFragmentByTag(1);
        }else if(currentFragment instanceof DrugReactionDetailsFragment){
            loadFragmentByTag(2);
        }else if(currentFragment instanceof DrugInfoFragment){
            loadFragmentByTag(3);
        }else {
            loadFragmentByTag(4);
        }


    }

    private void loadFragmentByTag(int tagNo){
        Bundle bundle = new Bundle();
        if (null != report) {
            bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
        }
        switch (tagNo){
            case 2:
                DrugReactionDiagnosisDetailsFragment personDetailsFragment = new DrugReactionDiagnosisDetailsFragment();
                personDetailsFragment.setArguments(bundle);
                FragmentManager fragmentManager1 = getSupportFragmentManager();
                fragmentManager1.beginTransaction()
                        .replace(R.id.incident_report_form_container, personDetailsFragment,"PersonDetailsFragment")
                        .commit();
                break;
            case 3:
                DrugReactionDetailsFragment reportedByDetailsFragment = new DrugReactionDetailsFragment();
                reportedByDetailsFragment.setArguments(bundle);
                FragmentManager fragmentManager2 = getSupportFragmentManager();
                fragmentManager2.beginTransaction()
                        .replace(R.id.incident_report_form_container, reportedByDetailsFragment,"FirstFragment")
                        .commit();
                break;
            case 4:
                DrugInfoFragment drugInfo = new DrugInfoFragment();
                drugInfo.setArguments(bundle);
                FragmentManager fragmentManager3 = getSupportFragmentManager();
                fragmentManager3.beginTransaction()
                        .replace(R.id.incident_report_form_container, drugInfo,"FirstFragment")
                        .commit();
                break;
            case 1:
            default:
                PatientDetailsFragment detailsFragment = new PatientDetailsFragment();
                detailsFragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.incident_report_form_container, detailsFragment,"DetailsFragment")
                        .commit();


        }
    }
    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }

    }

}
