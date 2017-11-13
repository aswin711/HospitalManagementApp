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
import android.widget.Button;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.core.modal.event.drugreaction.DrugInfo;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.util.CalenderUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;


public class DrugInfoFragment extends Fragment implements
        DatePickerDialog.OnDateSetListener {

    protected View fragmentView;

    @Bind(R.id.incident_details_save)
    protected Button saveDetailsBtn;

    @Bind(R.id.suspected_drug)
    protected MaterialEditText drugName;

    @Bind(R.id.drug_dose)
    protected MaterialEditText drugDose;

    @Bind(R.id.drug_frequency)
    protected MaterialEditText drugFreequency;

    @Bind(R.id.drug_route)
    protected MaterialEditText drugRoute;


    @Bind(R.id.drug_dt_started)
    protected MaterialEditText drugStartedDt;
    @Bind(R.id.drug_dt_started_btn)
    protected com.rey.material.widget.Button drugStartedDtBtn;

    @Bind(R.id.drug_dt_ceased)
    protected MaterialEditText drugCeasedDt;
    @Bind(R.id.drug_dt_ceased_btn)
    protected com.rey.material.widget.Button drugCeasedDtBtn;

    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    protected EventBus eventBus;

    private AdverseDrugEvent report;
    private DrugInfo drugInfo;



    public DrugInfoFragment() {
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
        fragmentView = inflater.inflate(R.layout.fragment_drug_details, container, false);
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
                eventBus.post(getString(R.string.save_btn_clicked));
            }
        });
        initDatepicker(drugStartedDtBtn,"Set drug started date", "Start");
        initDatepicker(drugCeasedDtBtn,"Set drug ceased date", "Ceased");
        return fragmentView;
    }


    @Override
    public void onDestroyView() {
        eventBus.unregister(this);
        super.onDestroyView();
    }

    private void initScreen() {
        if (null != report) {
            drugInfo = report.getSuspectedDrug();
            if (null == drugInfo) {
                //TODO Please refer this line
                List<DrugInfo> drugInfoList = databaseHelper.getDrugInfoByEventID(report.getId());
                if (null != drugInfoList) {
                    for (DrugInfo drugInfo1 : drugInfoList) {

                            drugInfo = drugInfo1;
                            break;

                    }
                }
                if (null == drugInfo) {
                    drugInfo = new DrugInfo();
                    report.setSuspectedDrug(drugInfo);
                } else {
                    drugName.setText(drugInfo.getDrug());
                    drugDose.setText(drugInfo.getDose());
                    drugRoute.setText(drugInfo.getRoute());
                    drugFreequency.setText(drugInfo.getFrequency());
                    if (null != drugInfo.getDateStarted()) {
                        drugStartedDt.setText(CalenderUtils.formatCalendarToString(drugInfo.getDateStarted(), Constants.Common.DATE_DISPLAY_FORMAT));
                    } else {
                        drugStartedDtBtn.setText("Set");
                    }
                    if (null != drugInfo.getDateCeased()) {
                        drugCeasedDt.setText(CalenderUtils.formatCalendarToString(drugInfo.getDateCeased(), Constants.Common.DATE_DISPLAY_FORMAT));
                    } else {
                        drugCeasedDtBtn.setText("Set");
                    }
                    saveDetailsBtn.setText("Update");
                }
            }
            initDatepicker(drugStartedDtBtn, "Date Started", "Started");
            initDatepicker(drugCeasedDtBtn, "Date Ceased", "Ceased");
        }
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
                        DrugInfoFragment.this,
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(true);
                dpd.vibrate(true);
                dpd.dismissOnPause(true);
                dpd.showYearPickerFirst(true);
                dpd.setTitle(title);
                //Setting max date
                dpd.setMaxDate(Calendar.getInstance());
                dpd.show(getActivity().getFragmentManager(), key+"Datepickerdialog");
            }
        });

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
        if ("CeasedDatepickerdialog".equals(view.getTag())) {
            drugInfo.setDateCeased(selectedDate);
            drugCeasedDt.setText(CalenderUtils.formatCalendarToString(drugInfo.getDateCeased(), Constants.Common.DATE_DISPLAY_FORMAT));

        } else {
            drugInfo.setDateStarted(selectedDate);
            drugStartedDt.setText(CalenderUtils.formatCalendarToString(drugInfo.getDateStarted(), Constants.Common.DATE_DISPLAY_FORMAT));

        }
    }

    public void saveEvent() {
        if (saveDrugDetails()) {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Reaction details added/updated added", Snackbar.LENGTH_LONG).show();
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
                report.setUpdated(Calendar.getInstance());
                drugInfo.setIsSuspectedDrug(true);
                drugInfo.setEventRef(report.getId());
                long id = databaseHelper.insertOrUpdateDrugInfo(drugInfo);
                if (0 < id) {
                    Timber.e("saveDrugDetails " + id);
                    report.setSuspectedDrug(drugInfo);
                }
            }
        }
    }

    public AdverseDrugEvent saveDraft(){

        if (!TextUtils.isEmpty(drugName.getText())) {
            drugInfo.setDrug(drugName.getText().toString().trim());
        }

        if (!TextUtils.isEmpty(drugDose.getText())) {
            drugInfo.setDose(drugDose.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(drugFreequency.getText())){
            drugInfo.setFrequency(drugFreequency.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(drugRoute.getText())){
            drugInfo.setRoute(drugRoute.getText().toString().trim());
        }

        return report;
    }

    private boolean saveDrugDetails() {
        if (!validateDrugDeatils()) {
            report.setUpdated(Calendar.getInstance());
            drugInfo.setIsSuspectedDrug(true);
            drugInfo.setEventRef(report.getId());
            long id = databaseHelper.insertOrUpdateDrugInfo(drugInfo);
            if (0 < id) {
                Timber.e("saveDrugDetails " + id);
                report.setSuspectedDrug(drugInfo);
                return true;
            }
        }
        return false;
    }

    private boolean validateDrugDeatils() {
        boolean error = false;
        if (TextUtils.isEmpty(drugName.getText())) {
            drugName.setError("Drug name is required");
            drugName.requestFocus();
            error = true;
        } else {
            drugInfo.setDrug(drugName.getText().toString().trim());
        }

        if (TextUtils.isEmpty(drugDose.getText())) {
            drugDose.setError("Drug dose is required");
            drugDose.requestFocus();
            error = true;
        } else {
            drugInfo.setDose(drugDose.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(drugFreequency.getText())){
            drugInfo.setFrequency(drugFreequency.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(drugRoute.getText())) {
            drugInfo.setRoute(drugRoute.getText().toString().trim());
        }

        if(null != drugInfo.getDateCeased() && null != drugInfo.getDateStarted()){
            Log.e("DrugInfo",drugInfo.getDateCeased()+"\n"+drugInfo.getDateStarted());
            if(!drugInfo.getDateCeased().after(drugInfo.getDateStarted())){
                drugCeasedDt.setText("");
                drugCeasedDt.setError("Ceased date must be same or after started date");
                drugCeasedDt.requestFocus();
                error = true;
            }else{
                Log.e("DrugInfo","Valid Dates");
            }

        }
        return error;
    }

    private void nextScreen() {
        DrugReactionReportedByDetailsFragment reportedByDetailsFragment = new DrugReactionReportedByDetailsFragment();
        if (null != report) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.INCIDENT_ITEM, report);
            reportedByDetailsFragment.setArguments(bundle);
        }
        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_report_form_container, reportedByDetailsFragment)
                .commit();
    }

    /**
     * Used to hide the soft input n fragment start
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hideSoftKeyBoard();

    }

    public void hideSoftKeyBoard(){
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}
