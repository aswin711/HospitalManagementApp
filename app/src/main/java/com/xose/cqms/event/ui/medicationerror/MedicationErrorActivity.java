package com.xose.cqms.event.ui.medicationerror;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.xose.cqms.event.BootstrapApplication;
import com.xose.cqms.event.R;
import com.xose.cqms.event.core.Constants;
import com.xose.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.xose.cqms.event.sqlite.DatabaseHelper;
import com.xose.cqms.event.ui.base.BootstrapFragmentActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.xose.cqms.event.core.Constants.Extra.HH_SESSION_ADD_OBSERVATION;
import static com.xose.cqms.event.core.Constants.Extra.INCIDENT_ITEM;
import static com.xose.cqms.event.core.Constants.Extra.INCIDENT_REF;

public class MedicationErrorActivity extends BootstrapFragmentActivity {


    @Inject
    protected DatabaseHelper databaseHelper;
    private MedicationError report;

    private Boolean doubleBackPressed = false;
    private Boolean editable = true;
    private FragmentBackPressed fragmentBackPressed;
    private FragmentBackPressed fragmentBackPressed2;
    private FragmentBackPressed fragmentBackPressed3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        setContentView(R.layout.activity_incident_report);
        fragmentBackPressed = (FragmentBackPressed) new MedicationErrorDetailsFragment();
        fragmentBackPressed2 = (FragmentBackPressed) new MedicationErrorPersonDetailsFragment();
        fragmentBackPressed3 = (FragmentBackPressed) new ErrorReportedByDetailsFragment();
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            report = (MedicationError) getIntent().getExtras().getSerializable(INCIDENT_ITEM);
            editable = getIntent().getExtras().getBoolean(HH_SESSION_ADD_OBSERVATION);
            if (null == report) {
                Long reportRef = getIntent().getExtras().getLong(INCIDENT_REF);
                Log.e("reportRef ", String.valueOf(reportRef));
                if (null != reportRef && 0 < reportRef) {
                    report = databaseHelper.getMedicationErrorById(reportRef);
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
                    if(getSupportFragmentManager().findFragmentByTag("DetailsFragment") != null){
                        fragmentBackPressed.onPressed(true,getApplicationContext());
                    }else if(getSupportFragmentManager().findFragmentByTag("SecondFragment") != null){
                        fragmentBackPressed2.onPressed(true,getApplicationContext());
                        //Toast.makeText(MedicationErrorActivity.this, "Toast from Activity", Toast.LENGTH_SHORT).show();
                    }else if(getSupportFragmentManager().findFragmentByTag("ReportByFragment") != null){
                        fragmentBackPressed3.onPressed(true,getApplicationContext());
                    }
                    doubleBackPressed = true;
                    onBackPressed();

                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            alertDialog.show();
        }else{
            super.onBackPressed();
        }
    }

    protected Activity getActivity() {
        return MedicationErrorActivity.this;
    }

    private void loadFragment() {
        MedicationErrorDetailsFragment detailsFragment = new MedicationErrorDetailsFragment();
        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            bundle.putBoolean(HH_SESSION_ADD_OBSERVATION,editable);
            detailsFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, detailsFragment,"DetailsFragment")
                .commit();
    }

    public interface FragmentBackPressed{
        void onPressed(Boolean status,Context context);
    }
}
