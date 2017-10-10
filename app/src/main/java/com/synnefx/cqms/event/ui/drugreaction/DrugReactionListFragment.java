package com.synnefx.cqms.event.ui.drugreaction;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.authenticator.LogoutService;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.sqlite.DataAccessException;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.base.ItemListFragment;
import com.synnefx.cqms.event.ui.base.ThrowableLoader;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SingleTypeAdapter;
import com.synnefx.cqms.event.util.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static com.synnefx.cqms.event.core.Constants.Extra.EDIT_REPORT_COMMAND;
import static com.synnefx.cqms.event.core.Constants.Extra.INCIDENT_ITEM;


public class DrugReactionListFragment extends ItemListFragment<AdverseDrugEvent> implements DrugReactionListAdapter.CustomButtonListener {

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
        //fragmentListener.viewFragment(2);
        eventBus.register(this);
        return inflater.inflate(R.layout.record_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(R.string.no_drug_reactions);
        // Used to hide the soft input n fragment start
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
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
        Log.e(TAG, "set click listener for footer");
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
        if (data.equals(getString(R.string.fab_clicked))) {
            startActivity(new Intent(getActivity(), DrugReactionActivity.class));
        }
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);
        eventBus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public Loader<List<AdverseDrugEvent>> onCreateLoader(int id, Bundle args) {
        final List<AdverseDrugEvent> initialItems = items;
        return new ThrowableLoader<List<AdverseDrugEvent>>(getActivity(), items) {

            @Override
            public List<AdverseDrugEvent> loadData() throws Exception {
                try {
                    if (getActivity() != null) {
                        String hospitalRef = PrefUtils.getFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
                        Log.e(TAG, "onCreateLoader - " + currentPage);
                        return databaseHelper.getAdverseDrugEventForDisplayByHospital(hospitalRef, currentPage);
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
    protected SingleTypeAdapter<AdverseDrugEvent> createAdapter(List<AdverseDrugEvent> items) {
        Log.e(TAG, "createAdapter");
        DrugReactionListAdapter adapter = new DrugReactionListAdapter(getActivity().getLayoutInflater(), items);
        adapter.setCustomButtonListner(DrugReactionListFragment.this);
        return adapter;
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        AdverseDrugEvent incident = ((AdverseDrugEvent) l.getItemAtPosition(position));
        showRecordActionPrompt(incident, view);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_drug_reactions;
    }

    @Override
    public void onButtonClickListner(int position, AdverseDrugEvent value) {
        Toast.makeText(getActivity(), "Button click " + value.getDepartment(),
                Toast.LENGTH_SHORT).show();

    }

    private boolean isTablet() {
        return UIUtils.isTablet(getActivity().getApplicationContext());
    }

    private void editSession(AdverseDrugEvent adverseDrugEvent) {
        Log.d("EditClicked",adverseDrugEvent.getId()+"");
        AdverseDrugEvent adverseDrugEvent1 = databaseHelper.getAdverseDrugEventById(adverseDrugEvent.getId());
        startActivity(new Intent(getActivity(), DrugReactionActivity.class).putExtra(INCIDENT_ITEM, adverseDrugEvent1).putExtra(EDIT_REPORT_COMMAND,true));
    }

    private boolean deleteSession(AdverseDrugEvent adverseDrugEvent) {
        databaseHelper.deleteAdverseDrugEventById(adverseDrugEvent.getId());
        return true;
    }

    private void openSession(AdverseDrugEvent adverseDrugEvent, boolean editable) {
        adverseDrugEvent = databaseHelper.getAdverseDrugEventById(adverseDrugEvent.getId());
        startActivity(new Intent(getActivity(), DrugReactionActivity.class).putExtra(INCIDENT_ITEM, adverseDrugEvent));
    }

    private void showRecordActionPrompt(final AdverseDrugEvent report, final View view) {
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
                                openSession(report, false);
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
