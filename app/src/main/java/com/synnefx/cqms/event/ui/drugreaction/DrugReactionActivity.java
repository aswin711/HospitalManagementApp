package com.synnefx.cqms.event.ui.drugreaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.base.BootstrapFragmentActivity;
import com.synnefx.cqms.event.ui.incident.IncidentReportViewActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.synnefx.cqms.event.core.Constants.Extra.EDIT_REPORT_COMMAND;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_REF;

public class DrugReactionActivity extends BootstrapFragmentActivity {


    @Inject
    protected DatabaseHelper databaseHelper;
    private AdverseDrugEvent report;

    private Boolean editable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        setContentView(R.layout.activity_incident_report);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            report = (AdverseDrugEvent) getIntent().getExtras().getSerializable(INCIDENT_ITEM);
            editable = getIntent().getBooleanExtra(EDIT_REPORT_COMMAND,false);
            if(!editable){
                //TODO
                startActivity(new Intent(this,IncidentReportViewActivity.class).putExtra(INCIDENT_ITEM,report));
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
                    //report = databaseHelper.getAdverseDrugEventById(reportRef);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_incident_report, menu);
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
        return DrugReactionActivity.this;
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

}
