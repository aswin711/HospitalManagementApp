package com.synnefx.cqms.event.ui.incident;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.ReportedBy;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.util.CalenderUtils;
import com.synnefx.cqms.event.util.ListViewer;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;

public class IncidentReportViewActivity extends AppCompatActivity {

    @Bind(R.id.medication_error_view_details_unit)
    protected TextView unit;
    @Bind(R.id.medication_error_view_details_incident_type)
    protected TextView incidentType;
    @Bind(R.id.medication_error_view_details_time)
    protected TextView time;
    @Bind(R.id.medication_error_view_details_incident_level)
    protected TextView level;
    @Bind(R.id.medication_error_view_details_incident_description)
    protected TextView description;
    @Bind(R.id.medication_error_view_details_corrective_action)
    protected TextView correctiveAction;


    @Bind(R.id.medication_error_view_person_involved_name)
    protected TextView personName;

    @Bind(R.id.medication_error_view_person_involved_type_patient)
    protected LinearLayout patientLayout;
    @Bind(R.id.medication_error_view_person_involved_type_patient_number)
    protected TextView patientNumber;
    @Bind(R.id.medication_error_view_person_involved_type_patient_gender)
    protected TextView patientGender;

    @Bind(R.id.medication_error_view_person_involved_type_staff)
    protected LinearLayout staffLayout;
    @Bind(R.id.medication_error_view_person_involved_type_staff_number)
    protected TextView staffNumber;
    @Bind(R.id.medication_error_view_person_involved_type_staff_designation)
    protected TextView staffDesignation;

    @Bind(R.id.medication_error_view_person_involved_type_visitor)
    protected LinearLayout visitorLayout;

    @Bind(R.id.medication_error_view_reported_by_name)
    protected TextView reportedByName;
    @Bind(R.id.medication_error_view_reported_by_designation)
    protected TextView reportedByDesignation;

    @Inject
    protected DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_report_view);
        ButterKnife.bind(this);
        BootstrapApplication.component().inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Incident Report View");

        IncidentReport report = (IncidentReport) getIntent().getSerializableExtra(INCIDENT_ITEM);

        Log.d(INCIDENT_ITEM, ListViewer.view(report));

        IncidentReport errorReport = databaseHelper.getIncidentReportById(report.getId());
        if(null == errorReport){
            return;
        }

        //Unit unit1 = errorReport.getUnit();

        //List<Unit> units = databaseHelper.getAllUnitsTypes(unit1.getHospitalID());
        unit.setText(errorReport.getDepartment());
        incidentType.setText(errorReport.getIncidentTypeName());
        //Toast.makeText(this, ""+unit1.get, Toast.LENGTH_SHORT).show();

        time.setText(CalenderUtils.formatCalendarToString(report.getIncidentTime(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));
        level.setText(report.getIncidentLevelCode()==1?"Near Miss":(report.getIncidentLevelCode()==2?"Actual Harm":""));
        description.setText(report.getDescription());
        correctiveAction.setText(report.getCorrectiveActionTaken());

        PersonInvolved personInvolved = null;
        Long personelRef = errorReport.getPersonInvolvedRef();
        if(null != personelRef && 0 < personelRef){
            personInvolved = databaseHelper.getPersonInvolvedById(personelRef);
        }

        if(null != personInvolved){
            personName.setText(personInvolved.getName());
            switch (personInvolved.getPersonnelTypeCode()){
                case 1:
                    patientLayout.setVisibility(View.VISIBLE);
                    visitorLayout.setVisibility(View.GONE);
                    staffLayout.setVisibility(View.GONE);
                    patientNumber.setText(personInvolved.getHospitalNumber());
                    int code = personInvolved.getGenderCode();
                    patientGender.setText(code==1?"Male":(code==2?"Female":(code==3?"Indeterminate":"Not stated/inadequately described")));
                    break;
                case 2:
                    patientLayout.setVisibility(View.GONE);
                    staffLayout.setVisibility(View.VISIBLE);
                    visitorLayout.setVisibility(View.GONE);
                    staffNumber.setText(personInvolved.getStaffId());
                    staffDesignation.setText(personInvolved.getDesignation());
                    break;
                case 3:
                    visitorLayout.setVisibility(View.VISIBLE);
                    patientLayout.setVisibility(View.GONE);
                    staffLayout.setVisibility(View.GONE);
                    break;
                default:
                    visitorLayout.setVisibility(View.GONE);
                    patientLayout.setVisibility(View.GONE);
                    staffLayout.setVisibility(View.GONE);
                    break;
            }
        }

        ReportedBy reportedBy = null;
        Long reportedByRef = errorReport.getReportedByRef();
        if(null != reportedByRef && 0 < reportedByRef){
            reportedBy = databaseHelper.getReproteeByID(reportedByRef);
        }
        if(null != reportedBy){
            reportedByName.setText(reportedBy.getLastName());
            reportedByDesignation.setText(reportedBy.getDesignation());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
