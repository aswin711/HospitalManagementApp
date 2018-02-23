package com.synnefx.cqms.event.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.sqlite.DataAccessException;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.view.util.SystemUiHider;
import com.synnefx.cqms.event.util.CalenderUtils;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.UIUtils;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.pushy.sdk.Pushy;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SplashscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Inject
    BootstrapServiceProvider serviceProvider;
    @Inject
    protected DatabaseHelper databaseHelper;

    @Bind(R.id.splash_container)
    protected RelativeLayout splashContainer;
    @Bind((R.id.imgLogo))
    protected ImageView logoHolder;
    @Bind((R.id.app_version))
    protected TextView versionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        //Pushy's internal notification listening service will restart itself,
        Pushy.listen(this);
        // Check whether the user has granted us the READ/WRITE_EXTERNAL_STORAGE permissions
        /*
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request both READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE so that the
            // Pushy SDK will be able to persist the device token in the external storage
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        */

        if (UIUtils.isTablet(this)) {
            setContentView(R.layout.activity_spashscreen);
        } else {
            setContentView(R.layout.activity_spashscreen);
        }
        ButterKnife.bind(this);

        //TODO check for any warning or notification from server
        try {
            PackageManager manager = getApplicationContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(
                    getApplicationContext().getPackageName(), 0);
            if (null != info) {
                String currentVersion = info.versionName;
                String latestVersion = PrefUtils.getLatestAppVersion(getApplicationContext());
                versionNumber.setText(currentVersion);
                //TODO check both version and appropreatly prompt for update
                if (!TextUtils.isEmpty(latestVersion) && !latestVersion.equalsIgnoreCase(currentVersion)) {
                    //Not latest version, request for update
                    showUpdatePrompt();
                }
            }
        } catch (Exception e) {

        }
        checkForDumpData();
        hideSpashScreen();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }

    private void hideSpashScreen() {
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashscreenActivity.this, MainActivity.class);
                startActivity(i);
                // close this activity
                finish();
            }
        }, AUTO_HIDE_DELAY_MILLIS);
    }

    private void showUpdatePrompt() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        int imageResource = android.R.drawable.ic_dialog_alert;
        alertDialog.setTitle("Update");
        alertDialog.setMessage("A latest version of this app is available. Please update");
        alertDialog.setIcon(imageResource);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                hideSpashScreen();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                hideSpashScreen();
            }
        });
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    private void checkForDumpData(){
        String currentDate = CalenderUtils.formatCalendarToString(Calendar.getInstance(), Constants.Common.DATE_DISPLAY_FORMAT);
        String lastCleanUpDate = PrefUtils.getLastCleanUpDate();
        if (!lastCleanUpDate.equals(currentDate)){
            //Find the date 2 months ago
            long DAY_IN_MS = 1000 * 60 * 60 * 24;
            long MON_IN_MS = 30 * DAY_IN_MS;

            Date cDate = new Date(Calendar.getInstance().getTimeInMillis()- 2*MON_IN_MS);
            Calendar cal = Calendar.getInstance();
            cal.setTime(cDate);

            Log.e("CheckForDumpData", CalenderUtils.formatCalendarToString(cal, Constants.Common.DATE_DISPLAY_FORMAT));
            try {
                databaseHelper.deleteIncidentReportsBeforeDate(cal);
                databaseHelper.deleteAdverseDrugReportsBeforeDate(cal);
               // databaseHelper.deleteMedicalErrorReportsBeforeDate(cal);
                //update new cleanup date
                PrefUtils.updateCleanUpDate(currentDate);
            } catch (DataAccessException e) {
                e.printStackTrace();
            }

        }else {
            Log.e("CheckForDumpData", "Cleaned on: "+lastCleanUpDate+"\n Current date:"+currentDate);
        }
    }

}
