package com.synnefx.cqms.event.ui.drugreaction;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.core.modal.event.drugreaction.DrugInfo;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.util.CalenderUtils;
import com.synnefx.cqms.event.util.ListViewer;
import com.synnefx.cqms.event.util.PrefUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;

public class DrugReactionReportViewActivity extends AppCompatActivity {


    //PatientDetailsFragment
    @Bind(R.id.drug_reaction_report_view_patient_name)
    protected TextView patientName;
    @Bind(R.id.drug_reaction_report_view_patient_num)
    protected TextView patientNumber;
    @Bind(R.id.drug_reaction_report_view_patient_type)
    protected TextView patientType;
    @Bind(R.id.drug_reaction_report_view_patient_dob)
    protected TextView patientDOB;
    @Bind(R.id.drug_reaction_report_view_patient_gender)
    protected TextView patientGender;
    @Bind(R.id.drug_reaction_report_view_patient_ht)
    protected TextView patientHt;
    @Bind(R.id.drug_reaction_report_view_patient_wt)
    protected TextView patientWt;

    //DrugReactionDiagnosisDetailsFragment
    @Bind(R.id.drug_reaction_report_view_diagnosis_unit)
    protected TextView diagnosisUnit;
    @Bind(R.id.drug_reaction_report_view_diagnosis_patient)
    protected TextView diagnosisPatient;
    @Bind(R.id.drug_reaction_report_view_diagnosis_toe)
    protected TextView diagnosisTime;
    @Bind(R.id.drug_reaction_report_view_diagnosis_consultant)
    protected TextView diagnosisConsultant;
    @Bind(R.id.drug_reaction_report_view_diagnosis_description)
    protected TextView diagnosisDescription;

    //DrugReactionDetailsFragment
    @Bind(R.id.drug_reaction_report_view_reaction_corrective)
    protected TextView reactionCorrectiveAction;
    @Bind(R.id.drug_reaction_report_view_reaction_outcome)
    protected TextView reactionOutcome;
    @Bind(R.id.drug_reaction_report_view_reaction_time)
    protected TextView reactionTime;
    @Bind(R.id.drug_reaction_report_view_reaction_time_layout)
    protected LinearLayout reactionTimeLayout;
    @Bind(R.id.drug_reaction_report_view_reaction_casesheet)
    protected TextView reactionCaseSheet;
    @Bind(R.id.drug_reaction_report_view_reaction_comments)
    protected TextView reactionComments;

    //DrugInfoFragment
    @Bind(R.id.drug_reaction_report_view_druginfo_name)
    protected TextView drugInfoSuspected;
    @Bind(R.id.drug_reaction_report_view_druginfo_dose)
    protected TextView drugInfoDose;
    @Bind(R.id.drug_reaction_report_view_druginfo_frequency)
    protected TextView drugInfoFrequency;
    @Bind(R.id.drug_reaction_report_view_druginfo_route)
    protected TextView drugInfoRoute;
    @Bind(R.id.drug_reaction_report_view_druginfo_started)
    protected TextView drugInfoStartedTime;
    @Bind(R.id.drug_reaction_report_view_druginfo_ceased)
    protected TextView drugInfoCeasedTime;

    //DrugReactionReportedByFragment
    @Bind(R.id.drug_reaction_report_view_reported_by_name)
    protected TextView reportedByName;
    @Bind(R.id.drug_reaction_report_view_reported_by_designation)
    protected TextView reportedByDesignation;


    private AdverseDrugEvent report;

    @Inject
    protected DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_reaction_report_view);
        setTitle("Adverse Drug Reaction Report");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);
        BootstrapApplication.component().inject(this);

        if (getIntent().getExtras() != null){
            report = (AdverseDrugEvent) getIntent().getExtras().get(INCIDENT_ITEM);
            report = databaseHelper.getAdverseDrugEventById(report.getId());

            //PatientDetails
            patientName.setText(report.getPersonInvolved().getName());
            patientNumber.setText(report.getPersonInvolved().getHospitalNumber());
            patientType.setText(report.getPersonInvolved().getPatientTypeCode()==1?"IP":"OP");
            patientHt.setText(report.getPersonInvolved().getHeight()+"");
            patientWt.setText(report.getPersonInvolved().getWeight()+"");
            patientDOB.setText(CalenderUtils.formatCalendarToString(report.getPersonInvolved().getDateOfBirthIndividual(), ""));

            switch (report.getPersonInvolved().getGenderCode()){
                case 1:
                    patientGender.setText("Male");
                    break;
                case 2:
                    patientGender.setText("Female");
                    break;
                case 3:
                    patientGender.setText("Indeterminate");
                    break;
                case 4:
                    patientGender.setText("Not stated/inadequately described");
                    break;
            }

            //DrugReactionDiagnosis
            diagnosisConsultant.setText(checkNullString(report.getPersonInvolved().getConsultantName()));
            diagnosisDescription.setText(report.getDescription());
            diagnosisPatient.setText(report.getPersonInvolved().getDiagnosis());
            diagnosisUnit.setText(report.getDepartment());
            diagnosisTime.setText(CalenderUtils.formatCalendarToString(report.getReactionDate(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));

            //DrugReactionDetails
            reactionCorrectiveAction.setText(report.getCorrectiveActionTaken());
            reactionComments.setText(checkNullString(report.getAdditionalInfo()));
            switch (report.getActionOutcomeCode()){
                case 1:
                    reactionOutcome.setText("Recovered");
                    reactionTimeLayout.setVisibility(View.VISIBLE);
                    reactionTime.setText(CalenderUtils.formatCalendarToString(report.getDateOfRecovery(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));
                    break;
                case 2:
                    reactionOutcome.setText("Not yet recovered");
                    reactionTimeLayout.setVisibility(View.GONE);
                    break;
                case 3:
                    reactionOutcome.setText("Unknown");
                    reactionTimeLayout.setVisibility(View.GONE);
                    break;
                case 4:
                    reactionOutcome.setText("Fatal");
                    reactionTimeLayout.setVisibility(View.VISIBLE);
                    reactionTime.setText(CalenderUtils.formatCalendarToString(report.getDateOfDeath(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));
                    break;
            }
            reactionCaseSheet.setText(report.isReactionAddedToCasesheet()?"Yes":"No");

            //DrugInfo
            List<DrugInfo> drugInfoList = databaseHelper.getDrugInfoByEventID(report.getId());
            DrugInfo drugInfo = new DrugInfo();
            if (null != drugInfoList) {
                for (DrugInfo drugInfo1 : drugInfoList) {
                    drugInfo = drugInfo1;
                    break;

                }
            }
            if (report.getSuspectedDrug()!=null){
                drugInfoSuspected.setText(report.getSuspectedDrug().getDrug());
                drugInfoDose.setText(report.getSuspectedDrug().getDose());
                drugInfoFrequency.setText(report.getSuspectedDrug().getFrequency());
                drugInfoRoute.setText(report.getSuspectedDrug().getRoute());
                drugInfoStartedTime.setText(CalenderUtils.formatCalendarToString(report.getSuspectedDrug().getDateStarted(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));
                drugInfoCeasedTime.setText(CalenderUtils.formatCalendarToString(report.getSuspectedDrug().getDateCeased(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));
            }else{
                Log.e("DRView","No suspected drug :\n"+ ListViewer.view(drugInfo));
                drugInfoSuspected.setText(drugInfo.getDrug());
                drugInfoDose.setText(drugInfo.getDose());
                drugInfoFrequency.setText(checkNullString(drugInfo.getFrequency()));
                drugInfoRoute.setText(checkNullString(drugInfo.getRoute()));
                drugInfoStartedTime.setText(checkNullString(CalenderUtils.formatCalendarToString(drugInfo.getDateStarted(), Constants.Common.DATE_TIME_DISPLAY_FORMAT)));
                drugInfoCeasedTime.setText(checkNullString(CalenderUtils.formatCalendarToString(drugInfo.getDateCeased(), Constants.Common.DATE_TIME_DISPLAY_FORMAT)));
            }



            //ReportedBy
            reportedByName.setText(report.getReportedBy().getLastName());
            reportedByDesignation.setText(checkNullString(report.getReportedBy().getDesignation()));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public String checkNullString(String data){
        if (data == null){
            return "N/A";
        }
        return data;
    }
}
