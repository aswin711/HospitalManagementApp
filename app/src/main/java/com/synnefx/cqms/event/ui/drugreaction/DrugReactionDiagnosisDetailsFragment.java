package com.synnefx.cqms.event.ui.drugreaction;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.util.CalenderUtils;
import com.synnefx.cqms.event.util.PrefUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;


public class DrugReactionDiagnosisDetailsFragment extends Fragment implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    protected View fragmentView;

    @Bind(R.id.incident_details_save)
    protected Button saveDetailsBtn;
    @Bind(R.id.incident_units)
    protected MaterialBetterSpinner unitsSpinner;

    @Bind(R.id.consultant_name)
    protected MaterialEditText consultantName;

    @Bind(R.id.patient_diagnosis)
    protected MaterialEditText diagnosis;

    @Bind(R.id.event_time_btn)
    protected Button eventTimeBtn;
    @Bind(R.id.event_time)
    protected MaterialEditText eventTime;

    ArrayAdapter<Unit> unitAdapter;




    @Inject
    protected DatabaseHelper databaseHelper;

    @Inject
    protected EventBus eventBus;

    private AdverseDrugEvent report;


    public DrugReactionDiagnosisDetailsFragment() {
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
        fragmentView = inflater.inflate(R.layout.fragment_reaction_diagnosis_details, container, false);
        ButterKnife.bind(this, fragmentView);
        eventBus.register(this);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            report = (AdverseDrugEvent) bundle.getSerializable(INCIDENT_ITEM);
            if (null == report) {
                Long reportRef = bundle.getLong(Constants.Extra.INCIDENT_REF, 0l);
                Log.e("reportRef ", String.valueOf(reportRef));
                if (0 < reportRef) {
                    report = (AdverseDrugEvent) bundle.getSerializable(INCIDENT_ITEM);
                }
            }
        }
        initScreen();
        saveDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEventDetails();
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
            if(!TextUtils.isEmpty(report.getPersonInvolved().getConsultantName())){
                consultantName.setText(report.getPersonInvolved().getConsultantName());
            }else{
                consultantName.requestFocus();
            }
            diagnosis.setText(report.getPersonInvolved().getDiagnosis());
            if (null != report.getIncidentTime()) {
                eventTimeBtn.setText("Change");
                eventTime.setText(CalenderUtils.formatCalendarToString(report.getIncidentTime(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));
            } else {
                eventTimeBtn.setText("Set");
            }
        }else{
            eventTimeBtn.setText("Set");
        }
        initDatepicker();
        setUnitSpinner();
    }


    private void setUnitSpinner() {
        String hospitalRef = PrefUtils.getFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
        List<Unit> units = new ArrayList<>();
        Unit unit = new Unit();
        unit.setId(0l);
        unit.setServerId(0l);
        unit.setName("Select Unit/Department");
        units.add(unit);
        units.addAll(databaseHelper.getAllUnitsTypesByStatus(hospitalRef, 1));
        unitAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, units);
        unitsSpinner.setAdapter(unitAdapter);

        Unit selectedUnit = new Unit();
        selectedUnit.setHospitalUUID(hospitalRef);
        selectedUnit.setServerId(report.getUnitRef());
        int pos = unitAdapter.getPosition(selectedUnit);
        if (pos >= 0) {
            //serviceSpinner.setSelection(pos);
            unitsSpinner.setText(report.getDepartment());
        }else{
            report.setUnitRef(0L);
        }

        unitsSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // String mSelectedText = adapterView.getItemAtPosition(position).toString();
                Unit selectedUnit = unitAdapter.getItem(position);
                if (null != selectedUnit && null != selectedUnit.getServerId() && 0 < selectedUnit.getServerId()) {
                    report.setUnitRef(selectedUnit.getServerId());
                    report.setDepartment(selectedUnit.getName());
                } else {
                    report.setUnitRef(0L);
                    report.setDepartment(null);
                }
            }
        });

    }

    private void initDatepicker() {
        eventTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar date = Calendar.getInstance();
                if (null != report && null != report.getIncidentTime()) {
                    date = report.getIncidentTime();
                }
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        DrugReactionDiagnosisDetailsFragment.this,
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(true);
                dpd.vibrate(true);
                dpd.dismissOnPause(true);
                dpd.showYearPickerFirst(true);
                // dpd.setAccentColor(Color.parseColor("#9C27B0"));
                dpd.setTitle("Select Incident time");
                //Setting max date
                dpd.setMaxDate(Calendar.getInstance());

                dpd.show(getActivity().getFragmentManager(), "EventDatepickerdialog");
            }
        });

    }


    @Override
    public void onClick(View view) {
        //if (enableSeconds.isChecked() && view.getId() == R.id.enable_seconds) enableMinutes.setChecked(true);
        //if (!enableMinutes.isChecked() && view.getId() == R.id.enable_minutes) enableSeconds.setChecked(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog eventTimeDpd = (DatePickerDialog) getActivity().getFragmentManager().findFragmentByTag("EventDatepickerdialog");
        if (eventTimeDpd != null) eventTimeDpd.setOnDateSetListener(this);
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, monthOfYear, dayOfMonth);
        if ("EventDatepickerdialog".equals(view.getTag())) {
            report.setReactionDate(selectedDate);
            eventTime.setText(CalenderUtils.formatCalendarToString(report.getIncidentTime(), Constants.Common.DATE_DISPLAY_FORMAT));
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(
                    DrugReactionDiagnosisDetailsFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
            );

            if (now.getTime().after(selectedDate.getTime())) {
                tpd.setMaxTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
            }
            tpd.setThemeDark(true);
            tpd.vibrate(true);
            tpd.dismissOnPause(true);
            tpd.enableSeconds(false);
            tpd.enableMinutes(true);
            // tpd.setAccentColor(Color.parseColor("#9C27B0"));
            tpd.setTitle("Select Time");
            tpd.setTimeInterval(1, 5);
            tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Timber.d("TimePicker", "Dialog was cancelled");
                }
            });
            tpd.show(getActivity().getFragmentManager(), "EventTimepickerdialog");
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String secondString = second < 10 ? "0" + second : "" + second;
        String time = hourString + "h" + minuteString + "m" + secondString + "s";
        String dateTime = eventTime.getText().toString();
        report.getIncidentTime().set(Calendar.MINUTE, minute);
        report.getIncidentTime().set(Calendar.HOUR_OF_DAY, hourOfDay);
        report.getIncidentTime().set(Calendar.SECOND, second);
        eventTime.setText(CalenderUtils.formatCalendarToString(report.getIncidentTime(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));
    }


    @Subscribe
    public void onEventListened(String data){
        if (data.equals(getString(R.string.save_draft))){
            if(saveDraft() != null){
                Toast.makeText(getActivity(),"Draft saved",Toast.LENGTH_SHORT).show();
                long id = databaseHelper.updateAdverseDrugEvent(saveDraft());
                if (0 < id){
                    databaseHelper.updateAdverseDrugEventPersonInvolved(saveDraft());
                }
            }
        }
    }


    public AdverseDrugEvent saveDraft(){

        if (null == report.getUnitRef() || 0 >= report.getUnitRef()) {
            report.setUnitRef(0l);
        }

        if (!TextUtils.isEmpty(consultantName.getText())){
            report.getPersonInvolved().setConsultantName(consultantName.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(diagnosis.getText())) {
            report.getPersonInvolved().setDiagnosis(diagnosis.getText().toString().trim());
        }

        return report;
    }

    public void saveEventDetails() {
        if (saveIncidentDetails()) {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Diagnosis details updated", Snackbar.LENGTH_LONG).show();
            nextScreen();
        } else {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Correct all validation errors", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean saveIncidentDetails() {
        if (!validateDeatils()) {
            // Long hospitalRef = PrefUtils.getLongFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
            //report.setHospital(hospitalRef);
            report.setUpdated(Calendar.getInstance());
            long reportId = databaseHelper.updateAdverseDrugEvent(report);
            if (0 < reportId) {
                long id = databaseHelper.updateAdverseDrugEventPersonInvolved(report);
                if (0 < id) {
                    Timber.e("saveIncidentDetails " + id);
                    return true;
                }
            }
        }
        return false;
    }


    private boolean validateDeatils() {
        boolean error = false;

        if (null == report.getUnitRef() || 0 >= report.getUnitRef()) {
            unitsSpinner.setError("Unit required");
            error = true;
            report.setUnitRef(0l);
            unitsSpinner.requestFocus();
        }else{

        }

        if (TextUtils.isEmpty(consultantName.getText())){
            consultantName.setError("Consultant name is required.");
            consultantName.requestFocus();
            error = true;
        }else {
            report.getPersonInvolved().setConsultantName(consultantName.getText().toString().trim());
        }
        if (TextUtils.isEmpty(diagnosis.getText())) {
            diagnosis.setError("Patient diagnosis required");
            diagnosis.requestFocus();
            error = true;
        } else {
            report.getPersonInvolved().setDiagnosis(diagnosis.getText().toString().trim());
        }
        if(null == report.getIncidentTime()){
            eventTime.setError("Incident time required");
        }
        return error;
    }

    private void nextScreen() {
        DrugReactionDetailsFragment reactionetailsFragment = new DrugReactionDetailsFragment();
        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            reactionetailsFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, reactionetailsFragment)
                .commit();
    }

    /**
     * Used to hide the soft input n fragment start
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}
