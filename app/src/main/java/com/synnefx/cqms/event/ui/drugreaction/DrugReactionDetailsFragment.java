package com.synnefx.cqms.event.ui.drugreaction;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.util.CalenderUtils;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.ViewUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;

public class DrugReactionDetailsFragment extends Fragment implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener {

    protected View fragmentView;

    @Bind(R.id.incident_details_save)
    protected Button saveDetailsBtn;

    @Bind(R.id.event_corrective_action)
    protected MaterialEditText correctiveAction;

    @Bind(R.id.action_outcome)
    protected MaterialBetterSpinner actionOutcomeSpinner;

    @Bind(R.id.recovered_dt_holder)
    protected LinearLayout recoveredDateHolder;
    @Bind(R.id.recovery_date)
    protected MaterialEditText recoveredDate;
    @Bind(R.id.recovered_on_btn)
    protected com.rey.material.widget.Button recoveryDtBtn;

    @Bind(R.id.death_dt_holder)
    protected LinearLayout deathDateHolder;
    @Bind(R.id.death_date)
    protected MaterialEditText deathDate;
    @Bind(R.id.death_date_btn)
    protected com.rey.material.widget.Button deathDtBtn;

    @Bind(R.id.reaction_in_casesheet_yes)
    protected RadioButton casesheetAddedYes;
    @Bind(R.id.reaction_in_casesheet_no)
    protected RadioButton casesheetAddedNo;

    @Bind(R.id.other_comments)
    protected MaterialEditText comments;

    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    protected EventBus eventBus;
    private AdverseDrugEvent report;

    ArrayAdapter<CharSequence> actionOutcomeAdapter;



    public DrugReactionDetailsFragment() {
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
        fragmentView = inflater.inflate(R.layout.fragment_reaction_details, container, false);
        ButterKnife.bind(this, fragmentView);
        eventBus.register(this);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            report = (AdverseDrugEvent) bundle.getSerializable(INCIDENT_ITEM);
            if (null == report) {
                Long reportRef = bundle.getLong(Constants.Extra.INCIDENT_REF, 0l);
                Log.e("reportRef ", String.valueOf(reportRef));
                if (0 < reportRef) {
                    report = databaseHelper.getAdverseDrugEventById(reportRef);
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
        ViewUtils.setGone(recoveredDateHolder, true);
        ViewUtils.setGone(deathDateHolder, true);

        casesheetAddedNo.setChecked(Boolean.FALSE);
        casesheetAddedYes.setChecked(Boolean.FALSE);

        if (null != report) {
            correctiveAction.setText(report.getCorrectiveActionTaken());
            if(report.isReactionAddedToCasesheet()){
                report.setReactionAddedToCasesheet(true);
                casesheetAddedYes.setChecked(true);
                casesheetAddedNo.setChecked(false);
            }else {
                casesheetAddedYes.setChecked(false);
                casesheetAddedNo.setChecked(true);
            }
            saveDetailsBtn.setText("Update");
            if (report.getComments()!=null){
                comments.setText(report.getComments());
            }
        } else {
            report.setIncidentTime(null);
            report.setStatusCode(0);
        }
        actionOutcomeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.action_outcomes, android.R.layout.simple_dropdown_item_1line);
        // Specify the layout to use when the list of choices appears
        //personTypeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        actionOutcomeSpinner.setAdapter(actionOutcomeAdapter);
        actionOutcomeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // String mSelectedText = adapterView.getItemAtPosition(position).toString();
                if (position > 0) {
                    CharSequence selectedItem = actionOutcomeAdapter.getItem(position);
                    if (null != selectedItem) {
                        report.setActionOutcomeCode(position);
                    }
                }
                setActionOutcomeViewVisibility(position);
            }
        });
        Integer actionOutcome = report.getActionOutcomeCode();
        if (null != actionOutcome && 0 < actionOutcome) {
            actionOutcomeSpinner.setText(actionOutcomeAdapter.getItem(actionOutcome));
        }
        setActionOutcomeViewVisibility(actionOutcome);
    }

    private void setActionOutcomeViewVisibility(Integer actionOutcome) {
        if (null == actionOutcome) {
            actionOutcome = 0;
        }
        switch (actionOutcome) {
            case 0:
                report.setActionOutcomeCode(0);
                hideAllActionOutcomeContainer();
                break;
            case 1:
                hideAllActionOutcomeContainer();
                ViewUtils.setGone(recoveredDateHolder, false);
                if(null != report.getDateOfRecovery()){
                    recoveredDate.setText(CalenderUtils.formatCalendarToString(report.getDateOfRecovery(),Constants.Common.DATE_DISPLAY_FORMAT));
                }else{
                    recoveredDate.setText("");
                }
                initDatepicker(recoveryDtBtn,"Set Date of Recovery", "Recovery");
                report.setDateOfDeath(null);
                break;
            case 4:
                hideAllActionOutcomeContainer();
                ViewUtils.setGone(deathDateHolder, false);
                if(null != report.getDateOfDeath()){
                    deathDate.setText(CalenderUtils.formatCalendarToString(report.getDateOfDeath(),Constants.Common.DATE_DISPLAY_FORMAT));
                }else{
                    deathDate.setText("");
                }
                initDatepicker(deathDtBtn,"Set Death date", "Death");
                report.setDateOfRecovery(null);
                break;
            default:
                hideAllActionOutcomeContainer();
                report.setDateOfDeath(null);
                report.setDateOfRecovery(null);
                break;
        }
    }

    private void hideAllActionOutcomeContainer() {
        ViewUtils.setGone(recoveredDateHolder, true);
        ViewUtils.setGone(deathDateHolder, true);
        recoveredDate.setText("");
        deathDate.setText("");
    }

    private void initDatepicker(com.rey.material.widget.Button dateSetter, final String title, final String key) {
        dateSetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar date = Calendar.getInstance();
                if (null != report && null != report.getIncidentTime()) {
                    date = report.getIncidentTime();
                }
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        DrugReactionDetailsFragment.this,
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(true);
                dpd.vibrate(true);
                dpd.dismissOnPause(true);
                dpd.showYearPickerFirst(true);
                // dpd.setAccentColor(Color.parseColor("#9C27B0"));
                dpd.setTitle(title);
                //Setting max date
                dpd.setMaxDate(Calendar.getInstance());

                dpd.show(getActivity().getFragmentManager(), key+"Datepickerdialog");
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
        if ("RecoveryDatepickerdialog".equals(view.getTag())) {
            report.setDateOfRecovery(selectedDate);
            recoveredDate.setText(CalenderUtils.formatCalendarToString(report.getDateOfRecovery(), Constants.Common.DATE_DISPLAY_FORMAT));
            report.setDateOfRecoveryStr(dayOfMonth+"/"+monthOfYear+"/"+year);
        } else if ("DeathDatepickerdialog".equals(view.getTag())) {
            report.setDateOfDeath(selectedDate);
            report.setDateOfDeathStr(dayOfMonth+"/"+monthOfYear+"/"+year);
            deathDate.setText(CalenderUtils.formatCalendarToString(report.getDateOfDeath(), Constants.Common.DATE_DISPLAY_FORMAT));
        }
    }

    public void saveEvent() {
        if (saveIncidentDetails()) {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Reaction details dded/updated added", Snackbar.LENGTH_LONG).show();
            nextScreen();
        } else {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Correct all validation errors", Snackbar.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onEventListened(String data){
        if (data.equals(getString(R.string.save_draft))){
            if(saveDraft() != null){
                Toast.makeText(getActivity(),"Draft saved",Toast.LENGTH_SHORT).show();
                databaseHelper.insertOrUpdateAdverseDrugReaction(saveDraft());
            }
        }
    }


    public AdverseDrugEvent saveDraft(){

        if (!TextUtils.isEmpty(correctiveAction.getText())) {
            report.setCorrectiveActionTaken(correctiveAction.getText().toString().trim());
        }

        if(casesheetAddedYes.isChecked()){
            report.setReactionAddedToCasesheet(true);
        }else if (casesheetAddedNo.isChecked()){
            report.setReactionAddedToCasesheet(false);
        }
        if (comments.getText() != null){
            report.setComments(comments.getText().toString().trim());
        }

        return report;
    }

    private boolean saveIncidentDetails() {
        if (!validateIncidentDeatils()) {
            String hospitalRef = PrefUtils.getFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
            report.setHospital(hospitalRef);
            if (0 == report.getStatusCode()) {
                report.setCreatedOn(Calendar.getInstance());
            }
            report.setUpdated(Calendar.getInstance());
            long id = databaseHelper.insertOrUpdateAdverseDrugReaction(report);
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
        } else if(correctiveAction.getText().length()<=10){
            correctiveAction.setError("Corrective action must have atleast 11 characters");
            correctiveAction.requestFocus();
            error = true;
        }
        else {
            report.setCorrectiveActionTaken(correctiveAction.getText().toString().trim());
        }

        if (null == report.getActionOutcomeCode() || 0 >= report.getActionOutcomeCode()) {
            actionOutcomeSpinner.setError("Action outcome required");
            error = true;
            report.setActionOutcomeCode(0);
            actionOutcomeSpinner.requestFocus();
        }else if (1== report.getActionOutcomeCode()){
            //Recovery date is mandatory
            if(null == report.getDateOfRecovery()){
                error = true;
                recoveredDate.setError("Date recovered required");
            }
        }else if (4== report.getActionOutcomeCode()){
            //death date is mandatory
            if(null == report.getDateOfDeath()){
                error = true;
                deathDate.setError("Death date required");
            }
        }
        if(casesheetAddedYes.isChecked()){
            report.setReactionAddedToCasesheet(true);
        }else if(casesheetAddedNo.isChecked()){
            report.setReactionAddedToCasesheet(false);
        }else{
            error = true;
            casesheetAddedYes.setError("This field is required");
        }
        if (comments.getText() != null){
            report.setComments(comments.getText().toString().trim());
        }
        return error;
    }

    private void nextScreen() {
        DrugInfoFragment drugInfoFragment = new DrugInfoFragment();
        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            drugInfoFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, drugInfoFragment)
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
