package com.synnefx.cqms.event.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.R.id;
import com.synnefx.cqms.event.R.layout;
import com.synnefx.cqms.event.R.string;
import com.synnefx.cqms.event.core.BootstrapService;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.ApiResponse;
import com.synnefx.cqms.event.core.modal.AuthenticationException;
import com.synnefx.cqms.event.core.modal.User;
import com.synnefx.cqms.event.events.NetworkErrorEvent;
import com.synnefx.cqms.event.events.UnAuthorizedErrorEvent;
import com.synnefx.cqms.event.sync.conf.ConfSyncContentProvider;
import com.synnefx.cqms.event.sync.drugreaction.DrugReactionSyncContentProvider;
import com.synnefx.cqms.event.sync.incident.IncidentReportSyncContentProvider;
import com.synnefx.cqms.event.sync.medicationerror.MedicationErrorSyncContentProvider;
import com.synnefx.cqms.event.ui.ImportConfigActivity;
import com.synnefx.cqms.event.ui.base.TextWatcherAdapter;
import com.synnefx.cqms.event.util.PrefUtils;
import com.synnefx.cqms.event.util.SafeAsyncTask;
import com.synnefx.cqms.event.util.Toaster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

/**
 * Activity to authenticate the user against an API (example API on Parse.com)
 */
public class BootstrapAuthenticatorActivity extends ActionBarAccountAuthenticatorActivity {

    /**
     * PARAM_CONFIRM_CREDENTIALS
     */
    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

    /**
     * PARAM_PASSWORD
     */
    public static final String PARAM_PASSWORD = "password";

    /**
     * PARAM_USERNAME
     */
    public static final String PARAM_USERNAME = "username";

    /**
     * PARAM_AUTHTOKEN_TYPE
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";


    private AccountManager accountManager;

    @Inject
    BootstrapService bootstrapService;

    @Inject
    protected BootstrapServiceProvider serviceProvider;

    @Inject
    Bus bus;

    @Bind(id.layout_email)
    protected TextInputLayout emailLayout;

    @Bind(id.layout_password)
    protected TextInputLayout passwordLayout;

    @Bind(id.et_email)
    protected EditText emailText;
    @Bind(id.et_password)
    protected EditText passwordText;
    @Bind(id.b_signin)
    protected Button signInButton;

    private final TextWatcher watcher = validationTextWatcher();

    private SafeAsyncTask<Boolean> authenticationTask;
    private SafeAsyncTask<Boolean> profileLoadTask;
    private String authToken;
    private String authTokenType;

    private ProgressDialog mProgressDialog;

    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean confirmCredentials = false;

    private String email;

    private String password;


    /**
     * In this instance the token is simply the sessionId returned from Parse.com. This could be a
     * oauth token or some other type of timed token that expires/etc. We're just using the parse.com
     * sessionId to prove the example of how to utilize a token.
     */
    private String token;

    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean requestNewAccount = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        BootstrapApplication.component().inject(this);

        accountManager = AccountManager.get(this);

        mProgressDialog = new ProgressDialog(this);

        final Intent intent = getIntent();
        email = intent.getStringExtra(PARAM_USERNAME);
        authTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        confirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);

        requestNewAccount = email == null;

        setContentView(layout.login_activity);

        ButterKnife.bind(this);

        emailText.addTextChangedListener(new MyTextWatcher(emailText));
        passwordText.addTextChangedListener(new MyTextWatcher(passwordText));

        final TextView signUpText = (TextView) findViewById(id.tv_signup);
        signUpText.setMovementMethod(LinkMovementMethod.getInstance());
        signUpText.setText(Html.fromHtml(getString(string.signup_link)));
        TextView recoverLink = (TextView) findViewById(id.recover_account);
        recoverLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // request your webservice here. Possible use of AsyncTask and ProgressDialog
                // show the result here - dialog or Toast
                Intent i = new Intent(BootstrapAuthenticatorActivity.this, ImportConfigActivity.class);
                startActivity(i);
            }

        });
        if (!isInternetAvaialable()) {
            showConnectionAlert();
        }
    }

    private List<String> userEmailAccounts() {
        final Account[] accounts = accountManager.getAccountsByType("com.google");
        final List<String> emailAddresses = new ArrayList<String>(accounts.length);
        for (final Account account : accounts) {
            emailAddresses.add(account.name);
        }
        return emailAddresses;
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable gitDirEditText) {
                updateUIWithValidation();
            }

        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        updateUIWithValidation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    private void updateUIWithValidation() {
        final boolean populated = populated(emailText) && populated(passwordText);
        signInButton.setEnabled(populated);
    }

    private boolean populated(final EditText editText) {
        return editText.length() > 0;
    }



    protected void showProgressSignIn(){
        mProgressDialog.setMessage("Signing in....");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (authenticationTask != null) {
                    authenticationTask.cancel(true);
                }
            }
        });
        hideProgressBar();
        mProgressDialog.show();
    }

    protected void showProgressFetchProfile(){
        if (mProgressDialog.isShowing()){
            mProgressDialog.setMessage("Fetching profile....");
        }
    }

    protected void hideProgressBar(){
        if (mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }

    @Subscribe
    public void onUnAuthorizedErrorEvent(UnAuthorizedErrorEvent unAuthorizedErrorEvent) {
        // Could not authorize for some reason.
        hideProgressBar();
        Toaster.showLong(BootstrapAuthenticatorActivity.this, R.string.message_bad_credentials);
    }

    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     * <p/>
     * Specified by android:onClick="handleLogin" in the layout xml
     *
     * @param view
     */
    public void handleLogin(final View view) {
        hideSoftKeyBoard();
        if (authenticationTask != null) {
            return;
        }

        if (requestNewAccount) {
            email = emailText.getText().toString();
        }

        password = passwordText.getText().toString();

        authenticationTask = new SafeAsyncTask<Boolean>(getApplicationContext()) {
            private String errorMessage = "Something went wrong";

            public Boolean call() throws Exception {

                final String query = String.format("%s=%s&%s=%s",
                        PARAM_USERNAME, email, PARAM_PASSWORD, password);
                String android_id = Settings.Secure.getString(this.getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                ApiResponse<String> loginResponse = bootstrapService.authenticate(email, password, android_id);
                if (null != loginResponse) {
                    token = loginResponse.getRecord();
                    if (!TextUtils.isEmpty(loginResponse.getError())) {
                        errorMessage = loginResponse.getError();
                    } else {
                        if (!TextUtils.isEmpty(token)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                // Retrofit Errors are handled inside of the {
                if (!(e instanceof AuthenticationException)) {
                    final Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause != null) {
                        Timber.e("Error while authentication - " + cause.getMessage(), cause);
                        Toaster.showLong(BootstrapAuthenticatorActivity.this, errorMessage);
                        Toaster.showLong(BootstrapAuthenticatorActivity.this, cause.getMessage());
                    }
                } else {
                    Timber.e("Error while authentication - " + e.getMessage(), e);
                    Toaster.showLong(BootstrapAuthenticatorActivity.this, e.getMessage());
                }
            }

            @Override
            public void onSuccess(final Boolean authSuccess) {
                onAuthenticationResult(authSuccess, errorMessage);
            }

            @Override
            protected void onFinally() throws RuntimeException {
                authenticationTask = null;
            }

            @Override
            protected void onPreExecute() throws Exception {
                showProgressSignIn();
            }
        };
        authenticationTask.execute();
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     *
     * @param result
     */
    protected void finishConfirmCredentials(final boolean result) {
        final Account account = new Account(email, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        accountManager.setPassword(account, password);
        final Intent intent = new Intent();
        fetchProfile();
        intent.putExtra(KEY_BOOLEAN_RESULT, result);

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

    }

    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. Also sets
     * the authToken in AccountManager for this account.
     */

    protected void finishLogin() {
        final Account account = new Account(email, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);

        authToken = token;

        if (requestNewAccount) {
            accountManager.addAccountExplicitly(account, password, null);
            accountManager.setAuthToken(account, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE, authToken);
        } else {
            accountManager.setPassword(account, password);
        }

        final Intent intent = new Intent();
        intent.putExtra(KEY_ACCOUNT_NAME, email);
        intent.putExtra(KEY_ACCOUNT_TYPE, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);


        if (authTokenType != null
                && authTokenType.equals(Constants.Auth.AUTHTOKEN_TYPE)) {
            intent.putExtra(KEY_AUTHTOKEN, authToken);
        }
        fetchProfile();
        /*if(!isPushRecordServiceRunning()){
            final Intent i = new Intent(this, PushRecordService.class);
            startService(i);
        }*/
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        //startActivity(new Intent(getApplicationContext(),MainActivity.class));
        //finish();

    }

    private void syncConfig() {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
	     * manual sync settings
	     */
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        final Account[] accounts = accountManager
                .getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        Timber.e("syncConfig Login");
        if (accounts.length > 0) {
            Log.d("TAG", "syncConfig .Scheduled");
            /*
             * Signal the framework to run your sync adapter. Assume that
             * app initialization has already created the account.
             */
            ContentResolver.requestSync(
                    accounts[0],
                    ConfSyncContentProvider.AUTHORITY,
                    settingsBundle);
        }
    }

    //Schedules for every 15 minutes
    private void scheduleSync() {
        Log.d("TAG", "scheduleSync...Scheduled periodically 15 minutes");
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, true);
        /*
	     * Request the sync for the default account, authority, and
	     * manual sync settings
	     */
        AccountManager accountManager = AccountManager.get(this);
        final Account[] accounts = accountManager
                .getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            ContentResolver.addPeriodicSync(
                    accounts[0],
                    IncidentReportSyncContentProvider.AUTHORITY,
                    Bundle.EMPTY,
                    15l);
            ContentResolver.addPeriodicSync(
                    accounts[0],
                    MedicationErrorSyncContentProvider.AUTHORITY,
                    Bundle.EMPTY,
                    20l);
            ContentResolver.addPeriodicSync(
                    accounts[0],
                    DrugReactionSyncContentProvider.AUTHORITY,
                    Bundle.EMPTY,
                    25l);
        }
    }

    protected void fetchProfile() {

        profileLoadTask = new SafeAsyncTask<Boolean>(getApplicationContext()) {
            private String errorMessage = "Something went wrong while setting profile";

            public Boolean call() throws Exception {
                String android_id = Settings.Secure.getString(this.getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                ApiResponse<User> userResponse = serviceProvider.getAuthenticatedService(BootstrapAuthenticatorActivity.this).getProfile();
                // ApiResponse<User> userResponse = bootstrapService.getProfile();
                if (null != userResponse) {
                    User user = userResponse.getRecord();
                    Log.d("User",user.getFirstName()+" "+user.getLastName());
                    if (!TextUtils.isEmpty(userResponse.getError())) {
                        errorMessage = userResponse.getError();
                    } else {
                        if (null != user) {

                            PrefUtils.saveToPrefs(getApplicationContext(), PrefUtils.PREFS_HOSP_ID, user.getAssociatedHospital());
                            PrefUtils.saveToPrefs(getApplicationContext(), PrefUtils.PREFS_USER_ID, user.getUserName());
                            PrefUtils.saveToPrefs(getApplicationContext(), PrefUtils.PREFS_USER_DISPLAY_NAME, user.getFullName());
                            PrefUtils.saveToPrefs(getApplicationContext(), PrefUtils.PREFS_HOSP_DISPLAY_NAME, user.getAssociatedHospitalName());
                            PrefUtils.saveToPrefs(getApplicationContext(),PrefUtils.PREF_USER_LOGGED_IN,true);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                // Retrofit Errors are handled inside of the {
                hideProgressBar();
                if (!(e instanceof AuthenticationException)) {
                    final Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause != null) {
                        Timber.e("Error while fetching profile - " + cause.getMessage(), cause);
                        Toaster.showLong(BootstrapAuthenticatorActivity.this, errorMessage);
                        Toaster.showLong(BootstrapAuthenticatorActivity.this, cause.getMessage());
                    }
                } else {
                    Timber.e("Error while fetching profile - " + e.getMessage(), e);
                    Toaster.showLong(BootstrapAuthenticatorActivity.this, e.getMessage());
                }
            }

            @Override
            public void onSuccess(final Boolean authSuccess) {
                //Toast.makeText(context, "Profile fetch completed.", Toast.LENGTH_SHORT).show();
                //importing begins here......

                //importing ends here

                syncConfig();
                scheduleSync();
            }

            @Override
            protected void onFinally() throws RuntimeException {
                hideProgressBar();
                importConfig();
                profileLoadTask = null;
            }

            @Override
            protected void onPreExecute() throws Exception {
                //showProgressBar(1);
               showProgressFetchProfile();
            }
        };
        profileLoadTask.execute();
    }


    public void importConfig(){
        Intent intent1 = new Intent(getApplicationContext(),ImportConfigActivity.class);
        startActivity(intent1);
        finish();
    }



    /**
     * Called when the authentication process completes (see attemptLogin()).
     *
     * @param result
     */
    public void onAuthenticationResult(final boolean result, String message) {
        if (result) {
            Timber.i("onAuthenticationResult: authentication success");
            if (!confirmCredentials) {
                finishLogin();
            } else {
                finishConfirmCredentials(true);
            }
        } else {
            hideProgressBar();
            Timber.i("onAuthenticationResult: failed to authenticate" + message);
            if (requestNewAccount) {
                Toaster.showLong(BootstrapAuthenticatorActivity.this,
                        string.message_auth_failed_new_account);
            } else {
                Toaster.showLong(BootstrapAuthenticatorActivity.this,
                        string.message_auth_failed);
            }
        }
    }


    @Subscribe
    public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
        Toaster.showLong(BootstrapAuthenticatorActivity.this, R.string.message_bad_connection);
    }

    private boolean validateEmail(String emailTextChanged) {
        String email = emailTextChanged.trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            emailLayout.setError(getString(R.string.err_msg_email));
            return false;
        } else {
            emailLayout.setError(null);
        }

        return true;
    }

    private boolean validatePassword(String passwordTextChanged) {

        if (passwordTextChanged.trim().isEmpty()) {
            passwordLayout.setError(getString(R.string.err_msg_password));
            return false;
        } else {
            passwordLayout.setError(null);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode((WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE));
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(emailLayout.getError() == null && validatePassword(passwordText.getText().toString())){
                signInButton.setEnabled(true);
            }else{
                signInButton.setEnabled(false);
            }
        }

        public void afterTextChanged(Editable editable) {

            switch (view.getId()) {

                case id.et_email:
                    validateEmail(editable.toString());
                    break;
                case id.et_password:
                    validatePassword(editable.toString());
                    break;
            }
        }
    }

    public void hideSoftKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
