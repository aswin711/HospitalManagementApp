package com.synnefx.cqms.event.ui.incident;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.util.ViewUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;


public class IncidentPersonDetailsFragment extends Fragment{

    protected View fragmentView;

    @Bind(R.id.incident_person_save)
    protected Button saveDetailsBtn;
    @Bind(R.id.person_involved_name)
    protected MaterialEditText personInvolvedName;
    @Bind(R.id.patient_number)
    protected MaterialEditText patientNumber;
    @Bind(R.id.staff_id_no)
    protected MaterialEditText staffIdNumber;
    @Bind(R.id.staff_designation)
    protected MaterialEditText staffDesignation;
    @Bind(R.id.person_types)
    protected MaterialBetterSpinner personTypeSpinner;

    @Bind(R.id.gender_grp)
    protected RadioGroup genederGroup;
    @Bind(R.id.gender_male)
    protected RadioButton radioMale;
    @Bind(R.id.gender_female)
    protected RadioButton radioFemale;
    @Bind(R.id.gender_indeterminate)
    protected RadioButton radioIndeterminate;
    @Bind(R.id.gender_not_stated)
    protected RadioButton radioNotStated;

    @Bind(R.id.patient_number_holder)
    protected FloatLabeledEditText patientNumberHolder;
    @Bind(R.id.staff_designation_holder)
    protected FloatLabeledEditText staffDesignNationHolder;
    @Bind(R.id.staff_id_no_holder)
    protected FloatLabeledEditText staffIdHolder;
    @Bind(R.id.gender_holder)
    protected LinearLayout genderHolder;



    ArrayAdapter<CharSequence> personTypeAdapter;

    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    EventBus eventBus;
    private IncidentReport report;
    private  PersonInvolved personInvolved;
    private Boolean editable = false;




    public IncidentPersonDetailsFragment() {
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
        fragmentView = inflater.inflate(R.layout.fragment_incident_person_details, container, false);
        ButterKnife.bind(this, fragmentView);
        eventBus.register(this);
        Bundle bundle = this.getArguments();
        report = null;
        if (bundle != null) {
            report = (IncidentReport) bundle.getSerializable(INCIDENT_ITEM);
            editable = bundle.getBoolean("editable");
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
            Long personInvolvedRef = report.getPersonInvolvedRef();
            if (null != personInvolvedRef && personInvolvedRef > 0) {
                personInvolved = databaseHelper.getPersonInvolvedById(personInvolvedRef);
            }
            report.setPersonInvolved(personInvolved);
            if (null != personInvolved) {
                personInvolvedName.setText(personInvolved.getName());
                // reportedByDesignation.setText(report.getCorrectiveActionTaken());
            }
        }
        if (null == personInvolved) {
            personInvolved = new PersonInvolved();
            personInvolved.setEventRef(report.getId());
        }

        personTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.type_person, android.R.layout.simple_dropdown_item_1line);
        // Specify the layout to use when the list of choices appears
        //personTypeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        personTypeSpinner.setAdapter(personTypeAdapter);
        personTypeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // String mSelectedText = adapterView.getItemAtPosition(position).toString();
                if (position > 0) {
                    CharSequence selectedItem = personTypeAdapter.getItem(position);
                    if (null != selectedItem) {
                        personInvolved.setPersonnelTypeCode(position);
                    }
                }
                setPersonelInfoViewVisibility(position);
            }
        });
        Integer selectedPersonType = personInvolved.getPersonnelTypeCode();
        if (null != selectedPersonType && 0 < selectedPersonType) {
            personTypeSpinner.setText(personTypeAdapter.getItem(selectedPersonType));
        }
        setPersonelInfoViewVisibility(selectedPersonType);
    }

    private void setPersonelInfoViewVisibility(Integer personelType) {
        if (null == personelType) {
            personelType = 0;
        }
        switch (personelType) {
            case 0:
                personInvolved.setPersonnelTypeCode(0);
                hideAllPersonelInfoContainer();
                break;
            case 1:
                hideAllPersonelInfoContainer();
                ViewUtils.setGone(patientNumberHolder, false);
                ViewUtils.setGone(genderHolder, false);
                patientNumber.setText(personInvolved.getHospitalNumber());
                if (null != personInvolved.getGenderCode()) {
                    switch (personInvolved.getGenderCode()) {
                        case 1:
                            radioMale.setChecked(true);
                            break;
                        case 2:
                            radioFemale.setChecked(true);
                            break;
                        case 3:
                            radioIndeterminate.setChecked(true);
                            break;
                        case 4:
                            radioNotStated.setChecked(true);
                            break;
                        default:
                            genederGroup.clearCheck();
                            break;
                    }
                }
                break;
            case 2:
                hideAllPersonelInfoContainer();
                ViewUtils.setGone(staffDesignNationHolder, false);
                ViewUtils.setGone(staffIdHolder, false);
                staffIdNumber.setText(personInvolved.getStaffId());
                staffDesignation.setText(personInvolved.getDesignation());
                break;
            case 3:
                hideAllPersonelInfoContainer();
                break;
        }
    }

    private void hideAllPersonelInfoContainer() {
        patientNumber.setText("");
        staffIdNumber.setText("");
        staffDesignation.setText("");
        genederGroup.clearCheck();
        ViewUtils.setGone(patientNumberHolder, true);
        ViewUtils.setGone(staffDesignNationHolder, true);
        ViewUtils.setGone(staffIdHolder, true);
        ViewUtils.setGone(genderHolder, true);
    }

    public void saveEventPersonDetails() {
        if (saveIncidentDetails()) {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Person Involved details updated", Snackbar.LENGTH_LONG).show();
            nextScreen();
        } else {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Correct all validation errors", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean saveIncidentDetails() {
        if (!validateDeatils()) {
            report.setUpdated(Calendar.getInstance());
            report.setPersonInvolved(personInvolved);
            long id = databaseHelper.updateIncidentPersonInvolved(report);
            if (id > 0) {
                return true;
            }
        }
        return false;
    }


    private boolean validateDeatils() {
        boolean error = false;
        if (TextUtils.isEmpty(personInvolvedName.getText())) {
            personInvolvedName.setError("Name of the person involved required");
            personInvolvedName.requestFocus();
            error = true;
        } else {
            personInvolved.setName(personInvolvedName.getText().toString().trim());
        }
        if (null == personInvolved.getPersonnelTypeCode() || 0 >= personInvolved.getPersonnelTypeCode()) {
            personTypeSpinner.setError("Type of person involved required");
            error = true;
        } else {
            switch (personInvolved.getPersonnelTypeCode()) {
                case 1:
                    if (TextUtils.isEmpty(patientNumber.getText())) {
                        patientNumber.setError("Patient number required");
                        error = true;
                    } else {
                        personInvolved.setHospitalNumber(patientNumber.getText().toString().trim());
                    }
                    if (radioMale.isChecked()) {
                        personInvolved.setGenderCode(1);
                    } else if (radioFemale.isChecked()) {
                        personInvolved.setGenderCode(2);
                    } else if (radioIndeterminate.isChecked()) {
                        personInvolved.setGenderCode(3);
                    } else if (radioNotStated.isChecked()) {
                        personInvolved.setGenderCode(4);
                    } else {
                        radioMale.setError("Gender required");
                        personInvolved.setGenderCode(0);
                    }
                    personInvolved.setStaffId(null);
                    personInvolved.setDesignation(null);
                    break;
                case 2:
                    if (TextUtils.isEmpty(staffIdNumber.getText())) {
                        staffIdNumber.setError("Patient number required");
                        error = true;
                    } else {
                        personInvolved.setStaffId(staffIdNumber.getText().toString().trim());
                    }
                    if (TextUtils.isEmpty(staffDesignation.getText())) {
                        staffDesignation.setError("Staff designation required");
                        error = true;
                    } else {
                        personInvolved.setDesignation(staffDesignation.getText().toString().trim());
                    }
                    personInvolved.setHospitalNumber(null);
                    personInvolved.setGenderCode(null);
                    break;
                case 3:
                    personInvolved.setHospitalNumber(null);
                    personInvolved.setGenderCode(null);
                    personInvolved.setStaffId(null);
                    personInvolved.setDesignation(null);
                    break;
            }
        }
        return error;
    }

    @Subscribe
    public void onEventListened(String data){
        if(data.equals(getString(R.string.save_draft))){
            if (saveDraft() != null){
                databaseHelper.updateIncidentPersonInvolved(saveDraft());
            }
        }
    }

    public IncidentReport saveDraft(){
        personInvolved.setName(personInvolvedName.getText().toString());

        switch (personTypeSpinner.getText().toString()){
            case "Patient":
                personInvolved.setPersonnelTypeCode(1);
                personInvolved.setHospitalNumber(patientNumber.getText().toString());
                personInvolved.setGenderCode(radioMale.isChecked()?1:(radioFemale.isChecked()?2:(radioIndeterminate.isChecked()?3
                        :(radioNotStated.isChecked()?4:0))));
                break;
            case "Staff":
                personInvolved.setPersonnelTypeCode(2);
                personInvolved.setStaffId(staffIdNumber.getText().toString());
                personInvolved.setDesignation(staffDesignation.getText().toString());
                break;
            case "Visitor":
                personInvolved.setPersonnelTypeCode(3);
                break;
            default:
                personInvolved.setPersonnelTypeCode(0);

        }

        report.setUpdated(Calendar.getInstance());
        report.setPersonInvolved(personInvolved);
       return report;
    }

    private void nextScreen() {
        ReportedByDetailsFragment reportedByDetailsFragment = new ReportedByDetailsFragment();
        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            bundle.putBoolean("editable",editable);
            reportedByDetailsFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, reportedByDetailsFragment,"ThirdFragment")
                .commit();
    }
}
