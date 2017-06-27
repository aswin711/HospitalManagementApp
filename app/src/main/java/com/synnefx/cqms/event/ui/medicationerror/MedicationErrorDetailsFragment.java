package com.synnefx.cqms.event.ui.medicationerror;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.RadioButton;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.util.CalenderUtils;
import com.synnefx.cqms.event.util.PrefUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.synnefx.cqms.event.R.id.event_corrective_action;
import static com.synnefx.cqms.event.core.Constants.Extra.HH_SESSION_ADD_OBSERVATION;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MedicationErrorDetailsFragment} interface
 * to handle interaction events.
 * Use the {@link MedicationErrorDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicationErrorDetailsFragment extends Fragment implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{


    protected static View fragmentView;

    @Bind(R.id.incident_details_save)
    protected Button saveDetailsBtn;
    @Bind(R.id.incident_units)
    protected MaterialBetterSpinner unitsSpinner;
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
    @Bind(event_corrective_action)
    protected MaterialEditText correctiveAction;

    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    EventBus eventBus;
    private static MedicationError report;
    private Boolean editable = true;

    ArrayAdapter<Unit> unitAdapter;



    public static MedicationErrorDetailsFragment newInstance(String param1, String param2) {
        MedicationErrorDetailsFragment fragment = new MedicationErrorDetailsFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MedicationErrorDetailsFragment() {
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
            report = (MedicationError) bundle.getSerializable(INCIDENT_ITEM);
            editable = bundle.getBoolean(HH_SESSION_ADD_OBSERVATION);
            if (null == report) {
                Long reportRef = bundle.getLong(Constants.Extra.INCIDENT_REF, 0l);
                Log.e("reportRef ", String.valueOf(reportRef));
                if (0 < reportRef) {
                    report = databaseHelper.getMedicationErrorById(reportRef);
                }
            }
        }
        initScreen();
        saveDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEvent();
            }
        });
        return fragmentView;
    }



    // TODO: Rename method, update argument and hook method into UI event



    @Override
    public void onDetach() {
        super.onDetach();
        report = null;
    }

   /* @Override
    public void onPressed(Boolean status, Context context) {
       Log.d("SavedItems",report.toString());
        Toast.makeText(context, "First fragment", Toast.LENGTH_SHORT).show();

        if(fragmentView != null){
            saveTempDetails(context);

        }else{
            Log.d("Viewer","not accesible");
        }
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */



    private void initScreen() {
        setUnitSpinner();
        nearMiss.setChecked(Boolean.FALSE);
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
        Long hospitalRef = PrefUtils.getLongFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
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
                // String mSelectedText = adapterView.getItemAtPosition(position).toString();
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
        selectedUnit.setHospitalID(hospitalRef);
        selectedUnit.setServerId(report.getUnitRef());
        int pos = unitAdapter.getPosition(selectedUnit);
        if (pos >= 0) {
            //serviceSpinner.setSelection(pos);
            unitsSpinner.setText(report.getDepartment());
        }
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
                        MedicationErrorDetailsFragment.this,
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

    @Subscribe
    public void onEventListened(String data){
        if(data.equals(getString(R.string.save_draft))){
            if(saveDraft() != null){
                databaseHelper.insertOrUpdateMedicationError(saveDraft());
            }



        }

    }

    @Override
    public void onDestroyView() {
        eventBus.unregister(this);
        super.onDestroyView();
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
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    MedicationErrorDetailsFragment.this,
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



    public void saveEvent() {
        if (saveIncidentDetails()) {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), R.string.added_incident, Snackbar.LENGTH_LONG).show();
            nextScreen();
            //Intent intent = new Intent(getActivity(),
            //        CasesheetObservationActivity.class);
            //intent.putExtra(INCIDENT_ITEM, report);
            //startActivity(intent);
            //getActivity().finish();
        } else {

            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Correct all validation errors", Snackbar.LENGTH_LONG).show();

            }
    }

    private boolean saveIncidentDetails() {
        if (!validateIncidentDeatils()) {
            Long hospitalRef = PrefUtils.getLongFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
            report.setHospital(hospitalRef);
            if (0 == report.getStatusCode()) {
                report.setCreatedOn(Calendar.getInstance());
            }
            report.setUpdated(Calendar.getInstance());
            long id = databaseHelper.insertOrUpdateMedicationError(report);
            if (0 < id) {
                Timber.e("saveIncidentDetails " + id);
                report.setId(id);
                return true;
            }
        }
        return false;
    }

    public void saveTempDetails(Context context){
        DatabaseHelper databaseHelper1 = new DatabaseHelper(context);
        Long hospitalRef = PrefUtils.getLongFromPrefs(context, PrefUtils.PREFS_HOSP_ID, null);
        report.setHospital(hospitalRef);
        MaterialEditText description = (MaterialEditText) fragmentView.findViewById(R.id.event_description);
        report.setDescription(description.getText().toString().trim());
        MaterialEditText correctiveAction = (MaterialEditText) fragmentView.findViewById(R.id.event_corrective_action);
        report.setCorrectiveActionTaken(correctiveAction.getText().toString().trim());
        RadioButton actualMiss = (RadioButton) fragmentView.findViewById(R.id.incident_level_near_miss);
        RadioButton harmMiss = (RadioButton) fragmentView.findViewById(R.id.incident_level_harm);

        if(actualMiss.isChecked()){
            report.setIncidentLevelCode(1);
        }else if(harmMiss.isChecked()){
            report.setIncidentLevelCode(2);
        }else {
            report.setIncidentLevelCode(0);
        }
        if (0 == report.getStatusCode()){

                report.setCreatedOn(Calendar.getInstance());

        }
        report.setUpdated(Calendar.getInstance());
        long id = databaseHelper1.insertOrUpdateMedicationError(report);
        if (0 < id) {
            Timber.e("saveIncidentDetails " + id);
            report.setId(id);
        }


    }

    public MedicationError saveDraft(){
        Long hospitalRef = PrefUtils.getLongFromPrefs(getContext(), PrefUtils.PREFS_HOSP_ID, null);
        report.setHospital(hospitalRef);
        MaterialEditText description = (MaterialEditText) fragmentView.findViewById(R.id.event_description);
        report.setDescription(description.getText().toString().trim());
        MaterialEditText correctiveAction = (MaterialEditText) fragmentView.findViewById(R.id.event_corrective_action);
        report.setCorrectiveActionTaken(correctiveAction.getText().toString().trim());
        RadioButton actualMiss = (RadioButton) fragmentView.findViewById(R.id.incident_level_near_miss);
        RadioButton harmMiss = (RadioButton) fragmentView.findViewById(R.id.incident_level_harm);

        if(actualMiss.isChecked()){
            report.setIncidentLevelCode(1);
        }else if(harmMiss.isChecked()){
            report.setIncidentLevelCode(2);
        }else {
            report.setIncidentLevelCode(0);
        }
        if (0 == report.getStatusCode()){

            report.setCreatedOn(Calendar.getInstance());

        }
        report.setUpdated(Calendar.getInstance());
       return report;
    }

    private boolean validateIncidentDeatils() {
        boolean error = false;

            if (TextUtils.isEmpty(correctiveAction.getText())) {
                correctiveAction.setError("Corrective action required");
                correctiveAction.requestFocus();
                error = true;
            } else {
                report.setCorrectiveActionTaken(correctiveAction.getText().toString().trim());
            }

            if (TextUtils.isEmpty(description.getText())) {
                description.setError("Incident description required");
                error = true;
            } else if(description.getText().length()<10){
                description.setError("Incident description length must be of minimum 10 characters.");
                error = true;
        }else{
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
            if (null == report.getUnitRef() || 0 >= report.getUnitRef()) {
                unitsSpinner.setError("Unit required");
                error = true;
                report.setUnitRef(0l);
                unitsSpinner.requestFocus();

        }



        return error;
    }

    private void nextScreen() {
        MedicationErrorPersonDetailsFragment personDetailsFragment = new MedicationErrorPersonDetailsFragment();

        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            bundle.putBoolean("editable",editable);
            personDetailsFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, personDetailsFragment,"SecondFragment")
                .commit();
    }
}
