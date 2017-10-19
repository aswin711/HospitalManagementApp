package com.synnefx.cqms.event.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.BootstrapService;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.events.NetworkErrorEvent;
import com.synnefx.cqms.event.services.gcm.QuickstartPreferences;
import com.synnefx.cqms.event.sqlite.DataAccessException;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.sync.drugreaction.DrugReactionSyncContentProvider;
import com.synnefx.cqms.event.sync.incident.IncidentReportSyncContentProvider;
import com.synnefx.cqms.event.sync.medicationerror.MedicationErrorSyncContentProvider;
import com.synnefx.cqms.event.ui.base.BootstrapActivity;
import com.synnefx.cqms.event.ui.base.NavigationDrawerFragment;
import com.synnefx.cqms.event.ui.drugreaction.DrugReactionListFragment;
import com.synnefx.cqms.event.ui.incident.IncidentReportListFragment;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorListFragment;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SafeAsyncTask;
import com.synnefx.cqms.event.util.ServiceUtils;
import com.synnefx.cqms.event.util.Toaster;
import com.synnefx.cqms.event.util.UIUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.pushy.sdk.Pushy;
import timber.log.Timber;


/**
 * Initial activity for the application.
 * <p/>
 * If you need to remove the authentication from the application please see
 * {@link com.synnefx.cqms.event.authenticator.ApiKeyProvider#getAuthKey(android.app.Activity)}
 */
public class MainActivity extends BootstrapActivity implements NavigationView.OnNavigationItemSelectedListener{

    @Inject
    BootstrapServiceProvider serviceProvider;

    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    EventBus eventBus;
    @Inject
    IncidentReportListFragment incidentReportListFragment;
    @Inject
    MedicationErrorListFragment medicationErrorListFragment;
    @Inject
    DrugReactionListFragment drugReactionListFragment;

    private boolean userHasAuthenticated = false;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private CharSequence title;
    private NavigationDrawerFragment navigationDrawerFragment;
    private FloatingActionButton floatingActionButton;

    private List<IncidentReport> incidentReports;
    private Handler handler;
    private IncidentReportContentObserver incidentReportContentObserver;
    private ProgressDialog mProgressDialog;

    /**
     * BroadcastReceiver used in service for Gcm registration.
     */
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    private int fragmentId;

    private boolean userLoggedIn = false;

    private View header;
    private NavigationView mNavigationView;




        class IncidentReportContentObserver extends ContentObserver {

        public IncidentReportContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("TAG", "Task content changed!");
            ;
        }

    }

    private void loadModel() {
        try {
            String hospitalRef = PrefUtils.getFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
            incidentReports = databaseHelper.getIncidentReportForDisplayByHospital(hospitalRef, 0);
        } catch (Exception e) {

        }
    }

    // Set up content observer for our content provider
    private void registerContentObservers() {
        ContentResolver cr = getContentResolver();
        handler = new Handler();
        incidentReportContentObserver = new IncidentReportContentObserver(handler);
        cr.registerContentObserver(IncidentReportSyncContentProvider.CONTENT_URI, true,
                incidentReportContentObserver);
    }

    private void unregisterContentObservers() {
        ContentResolver cr = getContentResolver();
        if (incidentReportContentObserver != null) { // just paranoia
            cr.unregisterContentObserver(incidentReportContentObserver);
            incidentReportContentObserver = null;
            handler = null;
        }
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        if (isTablet()) {
            setContentView(R.layout.main_activity);
        } else {
            setContentView(R.layout.main_activity);

        }

        userLoggedIn = false;

        getSupportActionBar().setElevation(0);
        // View injection with Butterknife
        ButterKnife.bind(this);

        // Set up navigation drawer
        title = drawerTitle = getTitle();

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this,drawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        mNavigationView.setNavigationItemSelectedListener(this);

        //setting margin for pre-lollipop devices
        if(isPreLollipop()){
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mNavigationView.getLayoutParams();
            params.setMargins(0,20-40,0,0);
            mNavigationView.setLayoutParams(params);
        }

        header = mNavigationView.getHeaderView(0);
        TextView user = (TextView) header.findViewById(R.id.navigation_drawer_list_header_user);
        TextView hospital = (TextView) header.findViewById(R.id.navigation_drawer_list_header_hospital);

        //Toast.makeText(this, , Toast.LENGTH_SHORT).show();

        user.setText(PrefUtils.getFromPrefs(getApplicationContext(), PrefUtils.PREFS_USER_DISPLAY_NAME, "User"));
        hospital.setText(PrefUtils.getFromPrefs(getApplicationContext(), PrefUtils.PREFS_HOSP_DISPLAY_NAME, "Hospital"));
        /*((TextView) navigationView.findViewById(R.id.navigation_drawer_list_header_user)).setText();
        ((TextView) navigationView.findViewById(R.id.navigation_drawer_list_header_hospital)).setText();*/


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        checkAuth();



        // GCM registration //
    }

    private void selectIncidentReport(){
        onNavigationItemSelected(mNavigationView.getMenu().getItem(0));
        mNavigationView.getMenu().getItem(0).setChecked(true);
    }

    @OnClick(R.id.floatingActionButton) void SelectEvent(){
        eventBus.post(getString(R.string.fab_clicked));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        TextView user = (TextView) header.findViewById(R.id.navigation_drawer_list_header_user);
        TextView hospital = (TextView) header.findViewById(R.id.navigation_drawer_list_header_hospital);

        //Toast.makeText(this, , Toast.LENGTH_SHORT).show();

        user.setText(PrefUtils.getFromPrefs(getApplicationContext(), PrefUtils.PREFS_USER_DISPLAY_NAME, "User"));
        hospital.setText(PrefUtils.getFromPrefs(getApplicationContext(), PrefUtils.PREFS_HOSP_DISPLAY_NAME, "Hospital"));

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // Handle navigation view item clicks here.
        //Bundle bundle = new Bundle();
        int id = menuItem.getItemId();
        FragmentManager manager = getSupportFragmentManager();

        if (id == R.id.nav_incident_report) {
            manager.beginTransaction().replace(R.id.container,incidentReportListFragment).commit();
            setTitle("Incident Report");

        } else if (id == R.id.nav_medication_error) {
            manager.beginTransaction().replace(R.id.container,medicationErrorListFragment).commit();
            setTitle("Medication Error");

        } else if (id == R.id.nav_adverse_drug_error) {
            manager.beginTransaction().replace(R.id.container,drugReactionListFragment).commit();
            setTitle("Adverse Drug Reaction");

        } else if (id == R.id.nav_sync_data) {
            initiateSync();
        } else if (id == R.id.nav_settings) {
            navigateToConfig();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isTablet() {
        return UIUtils.isTablet(this);
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isTablet()) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    protected Activity getActivity() {
        return MainActivity.this;
    }


    private void checkAuth() {
        new SafeAsyncTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                final BootstrapService svc = serviceProvider.getService(MainActivity.this);
                return svc != null;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
                if (e instanceof OperationCanceledException) {
                    // User cancelled the authentication process (back button, etc).
                    // Since auth could not take place, lets finish this activity.
                    finish();
                }
                Timber.e("error", e);
            }

            @Override
            protected void onSuccess(final Boolean hasAuthenticated) throws Exception {
                super.onSuccess(hasAuthenticated);
                Timber.e("onSuccess  Main");
                userHasAuthenticated = true;
                //importConfig();
                registerPushyOnServer();
                scheduleSync();
                selectIncidentReport();
                //initScreen();

                /*
                if(!isPushRecordServiceRunning()){
                    final Intent i = new Intent(MainActivity.this, PushRecordService.class);
                    startService(i);
                }
                */
            }

            @Override
            protected void onFinally() throws RuntimeException {
                hideLoading();
                initiateSync();
            }

            @Override
            protected void onPreExecute() throws Exception {
                showLoading();
            }
        }.execute();
    }


    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    public void registerPushyOnServer() {
        Timber.e("registerPushyOnServer  Main");
        //registerReceiver();
        new RegisterForPushNotificationsAsync().execute();

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {


        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                //menuDrawer.toggleMenu();
                return true;
           /* case R.id.timer:
                navigateToTimer();
                return true;
                */
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }


    @Override
    protected void onStart() {
        super.onStart();
        registerContentObservers();
    }

    @Override
    protected void onStop() {
        unregisterContentObservers();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }


    private void navigateToConfig() {
        final Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, 1);
    }

    //Schedules for every 15 minutes
    private void scheduleSync() {
        ServiceUtils.scheduleSync(this, IncidentReportSyncContentProvider.AUTHORITY, 600l);
        ServiceUtils.scheduleSync(this, MedicationErrorSyncContentProvider.AUTHORITY, 500l);
        ServiceUtils.scheduleSync(this, DrugReactionSyncContentProvider.AUTHORITY, 550l);
    }

    private void initiateSync() {
        Timber.e("initiateSync Main");
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            /*
	         * Request the sync for the default account, authority, and
	         * manual sync settings
	         */
        AccountManager accountManager = AccountManager.get(this);
        final Account[] accounts = accountManager
                .getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            ContentResolver.requestSync(accounts[0], IncidentReportSyncContentProvider.AUTHORITY, settingsBundle);
            ContentResolver.addPeriodicSync(accounts[0], IncidentReportSyncContentProvider.AUTHORITY, Bundle.EMPTY, 600);
            ContentResolver.requestSync(accounts[0], MedicationErrorSyncContentProvider.AUTHORITY, settingsBundle);
            ContentResolver.addPeriodicSync(accounts[0], MedicationErrorSyncContentProvider.AUTHORITY, Bundle.EMPTY, 500);
            ContentResolver.requestSync(accounts[0], DrugReactionSyncContentProvider.AUTHORITY, settingsBundle);
            ContentResolver.addPeriodicSync(accounts[0], DrugReactionSyncContentProvider.AUTHORITY, Bundle.EMPTY, 550);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        int imageResource = android.R.drawable.ic_dialog_alert;
        alertDialog.setTitle("Exit");
        alertDialog.setMessage("Want to exit?");
        alertDialog.setIcon(imageResource);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                MainActivity.super.onBackPressed();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    @Subscribe
    public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
        Toaster.showLong(MainActivity.this, R.string.message_bad_connection);
    }



    private class RegisterForPushNotificationsAsync extends AsyncTask<Void, Void, Exception> {

        protected Exception doInBackground(Void... params) {
            try {
                if (!PrefUtils.getTokenSentToServer()) {
                    // Assign a unique token to this device
                    String pusyToken = Pushy.register(getApplicationContext());
                    String deviceToken = PrefUtils.getDeviceToken();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    // Log it for debugging purposes
                    Log.d("MyApp", "Pushy device token: " + pusyToken);
                    // Send the token to your backend server via an HTTP GET request
                    //new URL("https://{YOUR_API_HOSTNAME}/register/device?token=" + deviceToken).openConnection();
                    if(TextUtils.isEmpty(pusyToken) && TextUtils.isEmpty(deviceToken)){
                        boolean sent = serviceProvider.getAuthenticatedService().doDeviceRegistration(deviceToken, pusyToken);
                        sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
                        PrefUtils.setTokenSentToServer(sent);
                    }
                }
            } catch (Exception exc) {
                // Return exc to onPostExecute
                return exc;
            }
            // Success
            return null;
        }

        @Override
        protected void onPostExecute(Exception exc) {
            // Failed?
            if (exc != null) {
                // Show error as toast message
                Timber.e(exc, "Push notification registration failed");
                Toast.makeText(getApplicationContext(), "Push notification registration failed", Toast.LENGTH_LONG).show();
                return;
            }

            // Succeeded, do something to alert the user
        }
    }

    public void showImportStatus(String msg, String subMsg, final int status){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        int imageResource = android.R.drawable.ic_dialog_alert;
        alertDialog.setTitle(msg);
        alertDialog.setMessage(subMsg);
        alertDialog.setIcon(imageResource);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });

        alertDialog.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {


            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

   private void showLoading(){
       hideLoading();
       mProgressDialog = UIUtils.showLoadingDialog(this);
   }

   private void hideLoading(){
       if (mProgressDialog!=null&&mProgressDialog.isShowing()){
           mProgressDialog.cancel();
       }
   }

    public boolean isPreLollipop(){
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP;
    }

}

