package com.xose.cqms.event.ui.incident;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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

import com.xose.cqms.event.BootstrapApplication;
import com.xose.cqms.event.BootstrapServiceProvider;
import com.xose.cqms.event.R;
import com.xose.cqms.event.authenticator.LogoutService;
import com.xose.cqms.event.core.modal.event.incident.IncidentReport;
import com.xose.cqms.event.sqlite.DataAccessException;
import com.xose.cqms.event.sqlite.DatabaseHelper;
import com.xose.cqms.event.ui.base.FragmentListener;
import com.xose.cqms.event.ui.base.ItemListFragment;
import com.xose.cqms.event.ui.base.ThrowableLoader;
import com.xose.cqms.event.util.PrefUtils;
import com.xose.cqms.event.util.SingleTypeAdapter;
import com.xose.cqms.event.util.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import static com.xose.cqms.event.core.Constants.Extra.HH_SESSION_ADD_OBSERVATION;
import static com.xose.cqms.event.core.Constants.Extra.HH_SESSION_ITEM;
import static com.xose.cqms.event.core.Constants.Extra.INCIDENT_ITEM;


public class IncidentReportListFragment extends ItemListFragment<IncidentReport> implements IncidentReportListAdapter.CustomButtonListener {

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
        eventBus.register(this);
        return inflater.inflate(R.layout.record_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(R.string.no_incidents);
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
        super.forceRefresh();
        super.onResume();
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
        if (data.equals(getString(R.string.fab_clicked))){
            startActivity(new Intent(getActivity(),IncidentReportActivity.class));
        }

    }
    @Override
    public void onDestroyView() {
        setListAdapter(null);
        eventBus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public Loader<List<IncidentReport>> onCreateLoader(int id, Bundle args) {
        final List<IncidentReport> initialItems = items;
        return new ThrowableLoader<List<IncidentReport>>(getActivity(), items) {

            @Override
            public List<IncidentReport> loadData() throws Exception {
                try {
                    if (getActivity() != null) {
                        Long hospitalRef = PrefUtils.getLongFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
                        Log.e(TAG, "onCreateLoader - " + currentPage);
                        return databaseHelper.getIncidentReportForDisplayByHospital(hospitalRef, currentPage);
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
    protected SingleTypeAdapter<IncidentReport> createAdapter(List<IncidentReport> items) {
        Log.e(TAG, "createAdapter");
        IncidentReportListAdapter adapter = new IncidentReportListAdapter(getActivity().getLayoutInflater(), items);
        adapter.setCustomButtonListner(IncidentReportListFragment.this);
        return adapter;
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        IncidentReport incident = ((IncidentReport) l.getItemAtPosition(position));
        //startActivity(new Intent(getActivity(), CasesheetAuditActivity.class).putExtra(INCIDENT_ITEM, audit));
        showRecordActionPrompt(incident, view);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_incidents;
    }

    @Override
    public void onButtonClickListner(int position, IncidentReport value) {
        Toast.makeText(getActivity(), "Button click " + value.getDepartment(),
                Toast.LENGTH_SHORT).show();

    }

    private boolean isTablet() {
        return UIUtils.isTablet(getActivity().getApplicationContext());
    }

    private void editSession(IncidentReport incidentReport) {
        startActivity(new Intent(getActivity(), IncidentReportActivity.class).putExtra(INCIDENT_ITEM, incidentReport).putExtra(HH_SESSION_ADD_OBSERVATION,true));
    }

    private boolean deleteSession(IncidentReport incidentReport) {
        databaseHelper.deleteIncidentReportById(incidentReport.getId());
        return true;
    }

    private void openSession(IncidentReport session) {
        startActivity(new Intent(getActivity(), IncidentReportActivity.class).putExtra(INCIDENT_ITEM, session).putExtra(HH_SESSION_ADD_OBSERVATION, false));
    }

    private void showRecordActionPrompt(final IncidentReport report, final View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                getActivity());
        // Setting Dialog Title
        alertDialog.setTitle("Select Action");
        // Setting Icon to Dialog
        // alertDialog.setIcon(R.drawable.ic_action_discard_dark);
        // Setting Positive "Yes" Button
        if (null != report) {
            if (!report.canEdit()) {
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
