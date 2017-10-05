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

import com.rengwuxian.materialedittext.MaterialEditText;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.core.modal.event.drugreaction.DrugInfo;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.util.CalenderUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DrugInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DrugInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DrugInfoFragment extends Fragment implements View.OnClickListener,
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

    private AdverseDrugEvent report;
    private DrugInfo drugInfo;

      private OnFragmentInteractionListener mListener;


    public static DrugInfoFragment newInstance(String param1, String param2) {
        DrugInfoFragment fragment = new DrugInfoFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    private void initScreen() {
        if (null != report) {
            drugInfo = report.getSuspectedDrug();
            if(null == drugInfo){
                List<DrugInfo> drugInfoList = databaseHelper.getDrugInfoByEventID(report.getId());
                if(null != drugInfoList){
                    for(DrugInfo drugInfo :drugInfoList){
                        if(drugInfo.isSuspectedDrug()){
                            drugInfo = drugInfo;
                            break;
                        }
                    }
                }
            }
            if(null == drugInfo){
                drugInfo = new DrugInfo();
                report.setSuspectedDrug(drugInfo);
            }else{
                drugName.setText(drugInfo.getDrug());
                drugDose.setText(drugInfo.getDose());
                drugRoute.setText(drugInfo.getRoute());
                drugFreequency.setText(drugInfo.getFrequency());
                if(null != drugInfo.getDateStarted()){
                    drugStartedDt.setText(CalenderUtils.formatCalendarToString(drugInfo.getDateStarted(), Constants.Common.DATE_DISPLAY_FORMAT));
                }else {
                    drugStartedDtBtn.setText("Set");
                }
                if(null != drugInfo.getDateCeased()){
                    drugCeasedDt.setText(CalenderUtils.formatCalendarToString(drugInfo.getDateCeased(), Constants.Common.DATE_DISPLAY_FORMAT));
                }else {
                    drugCeasedDtBtn.setText("Set");
                }
                initDatepicker(drugStartedDtBtn, "Date Started","Start");
                initDatepicker(drugCeasedDtBtn, "Date Ceased","Ceased");
                saveDetailsBtn.setText("Update");
            }
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
        if ("StartedDatepickerdialog".equals(view.getTag())) {
            report.setDateOfRecovery(selectedDate);
            drugStartedDt.setText(CalenderUtils.formatCalendarToString(report.getDateOfRecovery(), Constants.Common.DATE_DISPLAY_FORMAT));
        } else if ("CeasedDatepickerdialog".equals(view.getTag())) {
            report.setDateOfDeath(selectedDate);

            drugCeasedDt.setText(CalenderUtils.formatCalendarToString(report.getDateOfDeath(), Constants.Common.DATE_DISPLAY_FORMAT));
        }
    }

    public void saveEvent() {
        if (saveDrugDetails()) {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Reaction details dded/updated added", Snackbar.LENGTH_LONG).show();
            nextScreen();
        } else {
            Snackbar.make(getActivity().findViewById(R.id.footer_view), "Correct all validation errors", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean saveDrugDetails() {
        if (!validateDrugDeatils()) {
            report.setUpdated(Calendar.getInstance());
            drugInfo.setIsSuspectedDrug(true);
            drugInfo.setEventRef(report.getId());
            long id = databaseHelper.saveDrugInfo(drugInfo);
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
            drugName.setError("Corrective action required");
            drugName.requestFocus();
            error = true;
        } else {
            drugInfo.setDrug(drugName.getText().toString().trim());
        }

        if (TextUtils.isEmpty(drugDose.getText())) {
            drugDose.setError("Corrective action required");
            drugDose.requestFocus();
            error = true;
        } else {
            drugInfo.setDose(drugDose.getText().toString().trim());
        }
        if(null != drugInfo.getDateCeased() && null != drugInfo.getDateStarted()){
            if(drugInfo.getDateStarted().after(drugInfo.getDateCeased())){
                drugStartedDt.setError("Invalid Dates");
                drugCeasedDt.setError("Invalid Dates");
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
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}
