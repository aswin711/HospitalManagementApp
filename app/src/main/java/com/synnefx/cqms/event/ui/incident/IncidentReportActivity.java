package com.synnefx.cqms.event.ui.incident;

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
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.base.BootstrapFragmentActivity;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.ButterKnife;

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
