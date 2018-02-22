package com.synnefx.cqms.event.ui.incident;

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
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.IncidentType;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.base.BootstrapFragment;
import com.synnefx.cqms.event.util.CalenderUtils;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.ViewUtils;
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


public class IncidentDetailsFragment extends Fragment implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {


    protected  View fragmentView;

    @Bind(R.id.incident_details_save)
    protected Button saveDetailsBtn;
    @Bind(R.id.incident_units)
    protected MaterialBetterSpinner unitsSpinner;
    @Bind(R.id.incident_types)
    protected MaterialBetterSpinner incidentTypeSpinner;
    @Bind(R.id.incident_types_holder)
    protected LinearLayout incidentTypeHolder;
    @Bind(R.id.event_time_btn)
    protected Button eventTimeBtn;
    @Bind(R.id.event_time)
    protected MaterialEditText eventTime;
    @Bind(R.id.incident_level_near_miss)
    protected RadioButton nearMiss;
    @Bind(R.id.incident_level_harm)
    protected RadioButton actualHarm;
    @Bind(R.id.event_description)
    protected MaterialEditText description;
    @Bind(R.id.event_corrective_action)
    protected MaterialEditText correctiveAction;

    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    EventBus eventBus;
    private IncidentReport report;

    ArrayAdapter<Unit> unitAdapter;
    ArrayAdapter<IncidentType> incidentTypeAdapter;

    public IncidentDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_incident_details, container, false);
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
                saveEvent();
                eventBus.post(getString(R.string.save_btn_clicked));
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
        ViewUtils.setGone(incidentTypeHolder, false);
        setIncidentTypeSpinner();
        setUnitSpinner();
        nearMiss.setChecked(Boolean.TRUE);
        actualHarm.setChecked(Boolean.FALSE);
        if (null != report && null != report.getId() && 0 < report.getId()) {
            description.setText(report.getDescription());
            correctiveAction.setText(report.getCorrectiveActionTaken());
            if (null != report.getIncidentTime()) {
                eventTimeBtn.setText("Change");
                eventTime.setText(CalenderUtils.formatCalendarToString(report.getIncidentTime(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));
            } else {
                eventTimeBtn.setText("Set");
            }

            if (null != report.getIncidentLevelCode()) {
                if (1 == report.getIncidentLevelCode()) {
                    nearMiss.setChecked(Boolean.TRUE);
                } else if (2 == report.getIncidentLevelCode()) {
                    actualHarm.setChecked(Boolean.TRUE);
                }
            }
            saveDetailsBtn.setText("Update");
        } else {
            report.setIncidentTime(null);
            report.setStatusCode(0);
            eventTimeBtn.setText("Set");
        }
        initDatepicker();
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

        unitsSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Unit selectedUnit = unitAdapter.getItem(position);
                if (null != selectedUnit && null != selectedUnit.getServerId() && 0 < selectedUnit.getServerId()) {
                    report.setUnitRef(selectedUnit.getServerId());
                    report.setDepartment(selectedUnit.getName());
                } else {
                    report.setUnitRef(0l);
                    report.setDepartment(null);
                }
            }
        });

        Unit selectedUnit = new Unit();
        selectedUnit.setHospitalUUID(hospitalRef);
        selectedUnit.setServerId(report.getUnitRef());
        int pos = unitAdapter.getPosition(selectedUnit);
        if (pos >= 0) {
            unitsSpinner.setText(report.getDepartment());
        }
    }

    private void setIncidentTypeSpinner() {
        String hospitalRef = PrefUtils.getFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
        List<IncidentType> incidentTypes = new ArrayList<>();
        IncidentType incidentType = new IncidentType();
        incidentType.setId(0l);
        incidentType.setServerId(0l);
        incidentType.setIncidentType("Select Incident type");
        incidentTypes.add(incidentType);
        incidentTypes.addAll(databaseHelper.getAllIncidentTypesByStatus(hospitalRef, 1));
        incidentTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, incidentTypes);
        incidentTypeSpinner.setAdapter(incidentTypeAdapter);
        incidentTypeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                IncidentType selectedType = incidentTypeAdapter.getItem(position);
                if (null != selectedType && null != selectedType.getServerId() && 0 < selectedType.getServerId()) {
                    report.setIncidentTypeRef(selectedType.getServerId());
                    report.setIncidentType(selectedType.getServerId());
                    report.setIncidentTypeName(selectedType.getIncidentType());
                } else {
                    report.setIncidentTypeRef(0l);
                    report.setIncidentTypeName(null);
                }
            }
        });

        IncidentType selectedType = new IncidentType();
        selectedType.setHospitalUUID(hospitalRef);
        selectedType.setServerId(report.getIncidentTypeRef());
        int pos = incidentTypeAdapter.getPosition(selectedType);
        if (pos >= 0) {
            incidentTypeSpinner.setText(report.getIncidentTypeName());
        }
    }

    private void initDatepicker() {
        eventTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

    }

    private void openDatePicker(){
        Calendar date = Calendar.getInstance();
        if (null != report && null != report.getIncidentTime()) {
            date = report.getIncidentTime();
        }
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                IncidentDetailsFragment.this,
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
            report.setIncidentTime(selectedDate);
            eventTime.setText(CalenderUtils.formatCalendarToString(report.getIncidentTime(), Constants.Common.DATE_DISPLAY_FORMAT));
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(
                    IncidentDetailsFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
            );

            if (now.getTime().after(selectedDate.getTime()) && dayOfMonth == now.get(Calendar.DAY_OF_MONTH)) {
                tpd.setMaxTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)-1, now.get(Calendar.SECOND));
            }
            tpd.setThemeDark(true);
            tpd.vibrate(true);
            tpd.dismissOnPause(true);
            tpd.enableSeconds(false);
            tpd.enableMinutes(true);
            tpd.setTitle("Select Time");
            tpd.setTimeInterval(1, 1);
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
        report.getIncidentTime().set(Calendar.MINUTE, minute);
        report.getIncidentTime().set(Calendar.HOUR_OF_DAY, hourOfDay);
        report.getIncidentTime().set(Calendar.SECOND, second);
        eventTime.setText(CalenderUtils.formatCalendarToString(report.getIncidentTime(), Constants.Common.DATE_TIME_DISPLAY_FORMAT));
    }

    public void saveEvent() {
        if (saveIncidentDetails()) {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Incident added", Snackbar.LENGTH_LONG).show();
            nextScreen();

        } else {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Correct all validation errors", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean saveIncidentDetails() {
        if (!validateIncidentDeatils()) {
            String hospitalRef = PrefUtils.getFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
            report.setHospital(hospitalRef);
            if (0 == report.getStatusCode()) {
                report.setCreatedOn(Calendar.getInstance());
            }
            report.setUpdated(Calendar.getInstance());
            long id = databaseHelper.insertOrUpdateIncidentReport(report);
            if (0 < id) {
                Timber.e("saveIncidentDetails " + id);
                report.setId(id);
                return true;
            }
        }
        return false;
    }

    private boolean validateIncidentDeatils() {
        boolean error = false;
        if (TextUtils.isEmpty(correctiveAction.getText())) {
            correctiveAction.setError("Corrective action required");
            correctiveAction.requestFocus();
            error = true;
        }else if(correctiveAction.getText().toString().length() < 10){
            correctiveAction.setError("Incident description must have minimum 10 characters.");
            error = true;
        }
        else{
            report.setCorrectiveActionTaken(correctiveAction.getText().toString().trim());
        }

        if (TextUtils.isEmpty(description.getText())) {
            description.setError("Incident description required");
            error = true;
        }else if (description.getText().length() < 10){
            description.setError("Incident description must have minimum 10 characters.");
            description.requestFocus();
            error = true;
        }
        else {
            report.setDescription(description.getText().toString().trim());
        }

        if (nearMiss.isChecked()) {
            report.setIncidentLevelCode(1);
        } else if (actualHarm.isChecked()) {
            report.setIncidentLevelCode(2);
        } else {
            error = true;
            report.setIncidentLevelCode(0);
            nearMiss.setError("Incident level Required");
        }

        if (null == report.getIncidentTypeRef() || 0 >= report.getIncidentTypeRef()) {
            incidentTypeSpinner.setError("Incident type required");
            error = true;
            report.setIncidentTypeRef(0l);
            incidentTypeSpinner.requestFocus();
        }
        if (null == report.getUnitRef() || 0 >= report.getUnitRef()) {
            unitsSpinner.setError("Unit required");
            error = true;
            report.setUnitRef(0l);
            unitsSpinner.requestFocus();
        }

        if (null == report.getIncidentTime()){
            eventTime.setError("Time required");
            error = true;
            eventTime.requestFocus();
        }

        return error;
    }

    @Subscribe
    public void onEventListened(String data){
        if (data.equals(getString(R.string.save_draft))){
            if(saveDraft() != null){
                databaseHelper.insertOrUpdateIncidentReport(saveDraft());
            }
        }
    }

    //saving contents as draft
    public IncidentReport saveDraft(){
        String hospitalRef = PrefUtils.getFromPrefs(getContext(), PrefUtils.PREFS_HOSP_ID, null);

        report.setHospital(hospitalRef);
        if (!TextUtils.isEmpty(description.getText())) {
            report.setDescription(description.getText().toString().trim());
        }else {
            report.setDescription(null);
        }
        if (!TextUtils.isEmpty(correctiveAction.getText())) {
            report.setCorrectiveActionTaken(correctiveAction.getText().toString().trim());
        } else {
            report.setCorrectiveActionTaken(null);
        }
        List<IncidentType> types =  databaseHelper.getAllIncidentTypes(hospitalRef);
        if(!incidentTypeSpinner.getText().toString().isEmpty()){
            for (IncidentType type:types){
                if(type.getIncidentType().equals(incidentTypeSpinner.getText())){
                    report.setIncidentTypeRef(type.getServerId());
                    report.setIncidentType(type.getServerId());
                    report.setIncidentTypeName(type.getIncidentType());
                }
            }
        }else{
            report.setIncidentTypeRef(0l);
            report.setIncidentTypeName(null);
        }
        if(nearMiss.isChecked()){
            report.setIncidentLevelCode(1);
        }else if(actualHarm.isChecked()){
            report.setIncidentLevelCode(2);
        }else {
            report.setIncidentLevelCode(0);
        }
        report.setStatusCode(0);
        if (0 == report.getStatusCode()){

            report.setCreatedOn(Calendar.getInstance());

        }
        report.setUpdated(Calendar.getInstance());
        return report;
    }

    private void nextScreen() {
        IncidentPersonDetailsFragment personDetailsFragment = new IncidentPersonDetailsFragment();
        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            personDetailsFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, personDetailsFragment,"PersonDetailsFragment")
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
