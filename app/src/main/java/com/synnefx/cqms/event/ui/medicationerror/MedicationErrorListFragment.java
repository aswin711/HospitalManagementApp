package com.synnefx.cqms.event.ui.medicationerror;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.authenticator.LogoutService;
import com.synnefx.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.synnefx.cqms.event.sqlite.DataAccessException;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.base.ItemListFragment;
import com.synnefx.cqms.event.ui.base.ThrowableLoader;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SingleTypeAdapter;
import com.synnefx.cqms.event.util.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import static com.synnefx.cqms.event.core.Constants.Extra.HH_SESSION_ADD_OBSERVATION;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;


public class MedicationErrorListFragment extends ItemListFragment<MedicationError> implements MedicationErrorListAdapter.CustomButtonListener {

    private static final String TAG = "INRIST";
    @Inject
    protected BootstrapServiceProvider serviceProvider;
    @Inject
    protected LogoutService logoutService;

    @Inject
    protected DatabaseHelper databaseHelper;
    @Inject
    EventBus eventBus;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.record_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(R.string.no_medication_errors);

    }



    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
        /*getListAdapter()
                .addHeader(activity.getLayoutInflater()
                        .inflate(R.layout.incident_list_item_labels, null));*/
        View loadMore = getFooterView();
        getListAdapter().addFooter(getFooterView());
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNextPage();
            }
        });
        Log.e(TAG, "set click listener fo footer");
    }

    @Override
    public void onResume() {
        eventBus.register(this);
        super.forceRefresh();
        super.onResume();
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }

    @Override
    protected View getFooterView() {
        if (null == this.footerView) {
            this.footerView = getActivity().getLayoutInflater()
                    .inflate(R.layout.record_list_more_footer, null);
        }
        Log.e(TAG, "getFooterView");
        return this.footerView;
    }
    @Subscribe
    public void onEventListened(String data){
        if(data.equals(getString(R.string.fab_clicked))){
            startActivity(new Intent(getActivity(),MedicationErrorActivity.class).putExtra(HH_SESSION_ADD_OBSERVATION,true));
        }else if (data.equals(getString(R.string.force_refresh))){
            Toast.makeText(getContext(), "Entered!!", Toast.LENGTH_SHORT).show();
        }


    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onEvent(String data) {
        if (data.equals(getString(R.string.force_refresh))){
            Log.e("eventbus","Received");
            Toast.makeText(getContext(), "Entered!!", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onDestroyView() {
        setListAdapter(null);
        super.onDestroyView();
    }

    @Override
    public Loader<List<MedicationError>> onCreateLoader(int id, Bundle args) {
        final List<MedicationError> initialItems = items;
        return new ThrowableLoader<List<MedicationError>>(getActivity(), items) {

            @Override
            public List<MedicationError> loadData() throws Exception {
                try {
                    if (getActivity() != null) {
                        Long hospitalRef = PrefUtils.getLongFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
                        Log.e(TAG, "onCreateLoader - " + currentPage);
                        //Log.d("ItemsinAdapter", ListViewer.view(databaseHelper.getMedicationErrorForDisplayByHospital(hospitalRef, currentPage)));
                        return databaseHelper.getMedicationErrorForDisplayByHospital(hospitalRef, currentPage);
                    } else {
                        return Collections.emptyList();
                    }
                } catch (DataAccessException e) {
                    Activity activity = getActivity();
                    if (activity != null)
                        activity.finish();
                    return initialItems;
                }
            }
        };
    }

    @Override
    protected SingleTypeAdapter<MedicationError> createAdapter(List<MedicationError> items) {
        Log.e(TAG, "createAdapter");

        MedicationErrorListAdapter adapter = new MedicationErrorListAdapter(getActivity().getLayoutInflater(), items);
        adapter.setCustomButtonListner(MedicationErrorListFragment.this);
        return adapter;
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        MedicationError incident = ((MedicationError) l.getItemAtPosition(position));
        showRecordActionPrompt(incident, view);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_incidents;
    }

    @Override
    public void onButtonClickListner(int position, MedicationError value) {
        Toast.makeText(getActivity(), "Button click " + value.getDepartment(),
                Toast.LENGTH_SHORT).show();

    }

    private boolean isTablet() {
        return UIUtils.isTablet(getActivity().getApplicationContext());
    }

    private void editSession(MedicationError incidentReport) {
        startActivity(new Intent(getActivity(), MedicationErrorActivity.class).putExtra(INCIDENT_ITEM, incidentReport).putExtra(HH_SESSION_ADD_OBSERVATION,true));
    }

    private boolean deleteSession(MedicationError incidentReport) {
        databaseHelper.deleteMedicationErrorById(incidentReport.getId());
        return true;
    }

    private void openSession(MedicationError session) {
        startActivity(new Intent(getActivity(), MedicationErrorActivity.class).putExtra(INCIDENT_ITEM, session).putExtra(getString(R.string.view_details),true));
    }

    private void showRecordActionPrompt(final MedicationError report, final View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                getActivity());
        // Setting Dialog Title
        alertDialog.setTitle("Select Action");
        // Setting Icon to Dialog
        // alertDialog.setIcon(R.drawable.ic_action_discard_dark);
        // Setting Positive "Yes" Button
        if (null != report) {
            if (report.canEdit()) {
                alertDialog.setPositiveButton("Edit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                editSession(report);
                            }
                        });
            } else {
                alertDialog.setNeutralButton("View Details",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                openSession(report);
                            }
                        });
            }

            if (report.canDelete()) {
                // Setting Negative "NO" Button
                alertDialog.setNegativeButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (deleteSession(report)) {
                                    //ViewUtils.setGone(view, true);
                                    removeItem(report);
                                    Snackbar.make(view, "Record deleted", Snackbar.LENGTH_LONG).show();
                                } else {
                                    Snackbar.make(view, "Error while deleting record", Snackbar.LENGTH_LONG).show();
                                }
                                dialog.cancel();
                            }
                        });
            }
        }
        alertDialog.show();
    }

}
