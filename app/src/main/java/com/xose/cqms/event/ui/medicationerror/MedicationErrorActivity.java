package com.xose.cqms.event.ui.medicationerror;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.xose.cqms.event.BootstrapApplication;
import com.xose.cqms.event.R;
import com.xose.cqms.event.core.Constants;
import com.xose.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.xose.cqms.event.sqlite.DatabaseHelper;
import com.xose.cqms.event.ui.base.BootstrapFragmentActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.xose.cqms.event.core.Constants.Extra.INCIDENT_ITEM;
import static com.xose.cqms.event.core.Constants.Extra.INCIDENT_REF;

public class MedicationErrorActivity extends BootstrapFragmentActivity {


    @Inject
    protected DatabaseHelper databaseHelper;
    private MedicationError report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        setContentView(R.layout.activity_incident_report);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            report = (MedicationError) getIntent().getExtras().getSerializable(INCIDENT_ITEM);
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
                .replace(R.id.incident_report_form_container, detailsFragment)
                .commit();
    }
}
