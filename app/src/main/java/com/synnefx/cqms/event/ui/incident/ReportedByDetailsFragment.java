package com.synnefx.cqms.event.ui.incident;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.ReportedBy;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.sync.incident.IncidentReportSyncContentProvider;
import com.synnefx.cqms.event.util.ConnectionUtils;
import com.synnefx.cqms.event.util.ServiceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;


public class ReportedByDetailsFragment extends Fragment{

    protected View fragmentView;

    @Bind(R.id.incident_reported_by_save)
    protected Button saveDetailsBtn;
    @Bind(R.id.event_reported_by_name)
    protected MaterialEditText reportedByName;
    @Bind(R.id.event_reported_by_designation)
    protected MaterialEditText reportedByDesignation;



    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    EventBus eventBus;
    private IncidentReport report;
    private ReportedBy reportedBy;

    public ReportedByDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_reported_by_details, container, false);
        ButterKnife.bind(this, fragmentView);
        eventBus.register(this);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            report = (IncidentReport) bundle.getSerializable(INCIDENT_ITEM);
            if (null == report) {
                Long reportRef = bundle.getLong(Constants.Extra.INCIDENT_REF, 0l);
                Log.e("reportRef ", String.valueOf(reportRef));
                if (0 < reportRef) {
                    report = databaseHelper.getIncidentReportById(reportRef);
                }
            }
        }
        initScreen();
        saveDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEventPersonDetails();
            }
        });
        return fragmentView;
    }


    @Override
    public void onDestroyView() {
        eventBus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    private void initScreen() {
        if (null != report && null != report.getId() && 0 < report.getId()) {
            Long reportedByRef = report.getReportedByRef();
            if (null != reportedByRef && reportedByRef > 0) {
                reportedBy = databaseHelper.getReproteeByID(reportedByRef);
            }
            report.setReportedBy(reportedBy);
            if (null != reportedBy) {
                reportedByName.setText(reportedBy.getFullName());
                reportedByDesignation.setText(reportedBy.getDesignation());
            }
        }

        if (null == reportedBy) {
            reportedBy = new ReportedBy();
            reportedBy.setEventRef(report.getId());
            reportedBy.setReportedOn(Calendar.getInstance());
        }

    }

    public void saveEventPersonDetails() {
        if (saveIncidentDetails()) {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Incident added", Snackbar.LENGTH_LONG).show();
            report.setStatusCode(1);
            if (databaseHelper.completeIncidentReport(report) > 0) {
                Snackbar.make(getView().getRootView(), "Details updated", Snackbar.LENGTH_LONG).show();
                if (ConnectionUtils.isInternetAvaialable(getContext())) {
                    ServiceUtils.initiateSync(getContext(), IncidentReportSyncContentProvider.AUTHORITY);
                }
                //startActivity(new Intent(getActivity(), IncidentReportListActivity.class));
                getActivity().finish();
            } else {
                Snackbar.make(getView().getRootView(), "Error while updating", Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Correct all validation errors", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean saveIncidentDetails() {
        if (!validateDeatils()) {
            report.setUpdated(Calendar.getInstance());
            report.setReportedBy(reportedBy);
            long id = databaseHelper.updateIncidentReportedBy(report);
            if (id > 0) {
                return true;

            }
        }
        return false;
    }


    private boolean validateDeatils() {
        boolean error = false;
        if (TextUtils.isEmpty(reportedByName.getText())) {
            reportedByName.setError("Reported by (name) required");
            reportedByName.requestFocus();
            error = true;
        } else {
            reportedBy.setLastName(reportedByName.getText().toString().trim());
        }

        if (TextUtils.isEmpty(reportedByDesignation.getText())) {
            reportedByDesignation.setError("Reported by  designation required");
            error = true;
        } else {
            reportedBy.setDesignation(reportedByDesignation.getText().toString().trim());
        }
        return error;
    }

    @Subscribe
    public void onEventListened(String data){
        if(data.equals(getString(R.string.save_draft))){
            if(saveDraft() != null){
                databaseHelper.updateIncidentReportedBy(saveDraft());
            }
        }
    }

    //saving draft
    private IncidentReport saveDraft(){

        reportedBy.setLastName(reportedByName.getText().toString());
        reportedBy.setDesignation(reportedByDesignation.getText().toString());
        report.setUpdated(Calendar.getInstance());
        report.setReportedBy(reportedBy);
        return report;
    }

}
