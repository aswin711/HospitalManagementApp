package com.xose.cqms.event.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.xose.cqms.event.BootstrapApplication;
import com.xose.cqms.event.BootstrapServiceProvider;
import com.xose.cqms.event.R;
import com.xose.cqms.event.core.BootstrapService;
import com.xose.cqms.event.core.Constants;
import com.xose.cqms.event.core.modal.event.incident.IncidentReport;
import com.xose.cqms.event.events.NavItemSelectedEvent;
import com.xose.cqms.event.events.NetworkErrorEvent;
import com.xose.cqms.event.services.gcm.QuickstartPreferences;
import com.xose.cqms.event.sqlite.DatabaseHelper;
import com.xose.cqms.event.sync.drugreaction.DrugReactionSyncContentProvider;
import com.xose.cqms.event.sync.incident.IncidentReportSyncContentProvider;
import com.xose.cqms.event.sync.medicationerror.MedicationErrorSyncContentProvider;
import com.xose.cqms.event.ui.base.BootstrapActivity;
import com.xose.cqms.event.ui.base.NavigationDrawerFragment;
import com.xose.cqms.event.ui.drugreaction.DrugReactionListActivity;
import com.xose.cqms.event.ui.incident.IncidentReportListActivity;
import com.xose.cqms.event.ui.medicationerror.MedicationErrorListActivity;
import com.xose.cqms.event.util.PrefUtils;
import com.xose.cqms.event.util.SafeAsyncTask;
import com.xose.cqms.event.util.ServiceUtils;
import com.xose.cqms.event.util.Toaster;
import com.xose.cqms.event.util.UIUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import me.pushy.sdk.Pushy;
import timber.log.Timber;


/**
 * Initial activity for the application.
 * <p/>
 * If you need to remove the authentication from the application please see
 * {@link com.xose.cqms.event.authenticator.ApiKeyProvider#getAuthKey(android.app.Activity)}
 */
public class MainActivity extends BootstrapActivity {

    @Inject
    BootstrapServiceProvider serviceProvider;

    @Inject
    DatabaseHelper databaseHelper;

    private boolean userHasAuthenticated = false;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private CharSequence title;
    private NavigationDrawerFragment navigationDrawerFragment;

    private List<IncidentReport> incidentReports;
    private Handler handler;
    private IncidentReportContentObserver incidentReportContentObserver;

    /**
     * BroadcastReceiver used in service for Gcm registration.
     */
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    class IncidentReportContentObserver extends ContentObserver {

        public IncidentReportContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("TAG", "Task content changed!");
            initScreen();
        }

    }

    private void loadModel() {
        try {
            Long hospitalRef = PrefUtils.getLongFromPrefs(getActivity().getApplicationContext(), PrefUtils.PREFS_HOSP_ID, null);
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
            setContentView(R.layout.main_activity_tablet);
        } else {
            setContentView(R.layout.main_activity);
        }

        // View injection with Butterknife
        ButterKnife.bind(this);

        // Set up navigation drawer
        title = drawerTitle = getTitle();
        if (!isTablet()) {
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerToggle = new ActionBarDrawerToggle(
                    this,                    /* Host activity */
                    drawerLayout,           /* DrawerLayout object */
                    R.string.navigation_drawer_open,    /* "open drawer" description */
                    R.string.navigation_drawer_close) { /* "close drawer" description */

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    getSupportActionBar().setTitle(title);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    syncState();
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    getSupportActionBar().setTitle(drawerTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    syncState();
                }
            };

            if (!isTablet()) {
                drawerToggle.syncState();
            }

            // Set the drawer toggle as the DrawerListener
            drawerLayout.setDrawerListener(drawerToggle);
            navigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

            // Set up the drawer.
            navigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        checkAuth();

        // GCM registration //
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

    private void initScreen() {
        if (userHasAuthenticated) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new CarouselFragment())
                    .commit();
        }
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
                registerPushyOnServer();
                scheduleSync();
                initScreen();
                /*
                if(!isPushRecordServiceRunning()){
                    final Intent i = new Intent(MainActivity.this, PushRecordService.class);
                    startService(i);
                }
                */
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

        if (!isTablet() && drawerToggle.onOptionsItemSelected(item)) {
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

    @Subscribe
    public void onNavigationItemSelected(NavItemSelectedEvent event) {
        switch (event.getItemPosition()) {
            case 0:
                // Home
                // do nothing as we're already on the home screen.
                break;
            case 1:
                startActivity(new Intent(this, IncidentReportListActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, MedicationErrorListActivity.class));
                break;
            case 3:
                startActivity(new Intent(this, DrugReactionListActivity.class));
                break;
            case 4:
                navigateToConfig();
                break;
            case 5:
                initiateSync();
                break;
        }
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
                finish();
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
                    String deviceToken = Pushy.register(getApplicationContext());
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    // Log it for debugging purposes
                    Log.d("MyApp", "Pushy device token: " + deviceToken);
                    // Send the token to your backend server via an HTTP GET request
                    //new URL("https://{YOUR_API_HOSTNAME}/register/device?token=" + deviceToken).openConnection();

                    boolean sent = serviceProvider.getAuthenticatedService().doDeviceRegistration("android", deviceToken);
                    sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
                    PrefUtils.setTokenSentToServer(sent);

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

}
