package com.xose.cqms.event;

import android.preference.PreferenceActivity;

import com.xose.cqms.event.authenticator.BootstrapAuthenticatorActivity;
import com.xose.cqms.event.core.pushy.PushReceiver;
import com.xose.cqms.event.sync.conf.ConfSyncService;
import com.xose.cqms.event.sync.drugreaction.DrugReactionSyncService;
import com.xose.cqms.event.sync.incident.IncidentReportSyncService;
import com.xose.cqms.event.sync.medicationerror.MedicationErrorSyncService;
import com.xose.cqms.event.ui.ImportConfigActivity;
import com.xose.cqms.event.ui.MainActivity;
import com.xose.cqms.event.ui.SpashscreenActivity;
import com.xose.cqms.event.ui.base.BootstrapActivity;
import com.xose.cqms.event.ui.base.BootstrapFragmentActivity;
import com.xose.cqms.event.ui.base.NavigationDrawerFragment;
import com.xose.cqms.event.ui.drugreaction.DrugReactionActivity;
import com.xose.cqms.event.ui.drugreaction.DrugReactionDetailsFragment;
import com.xose.cqms.event.ui.drugreaction.DrugReactionListActivity;
import com.xose.cqms.event.ui.drugreaction.DrugReactionListFragment;
import com.xose.cqms.event.ui.drugreaction.DrugReactionPersonDetailsFragment;
import com.xose.cqms.event.ui.drugreaction.DrugReactionReportedByDetailsFragment;
import com.xose.cqms.event.ui.incident.ReportedByDetailsFragment;
import com.xose.cqms.event.ui.incident.IncidentDetailsFragment;
import com.xose.cqms.event.ui.incident.IncidentPersonDetailsFragment;
import com.xose.cqms.event.ui.incident.IncidentReportActivity;
import com.xose.cqms.event.ui.incident.IncidentReportListActivity;
import com.xose.cqms.event.ui.incident.IncidentReportListFragment;
import com.xose.cqms.event.ui.medicationerror.ErrorReportedByDetailsFragment;
import com.xose.cqms.event.ui.medicationerror.MedicationErrorActivity;
import com.xose.cqms.event.ui.medicationerror.MedicationErrorDetailsFragment;
import com.xose.cqms.event.ui.medicationerror.MedicationErrorListActivity;
import com.xose.cqms.event.ui.medicationerror.MedicationErrorListFragment;
import com.xose.cqms.event.ui.medicationerror.MedicationErrorPersonDetailsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                AndroidModule.class,
                BootstrapModule.class
        }
)
public interface BootstrapComponent {

    void inject(BootstrapApplication target);

    void inject(BootstrapAuthenticatorActivity target);

    void inject(MainActivity target);

    void inject(SpashscreenActivity target);

    void inject(NavigationDrawerFragment target);

    void inject(PreferenceActivity target);

    void inject(BootstrapFragmentActivity target);

    void inject(BootstrapActivity target);

    void inject(ImportConfigActivity target);

    void inject(ConfSyncService target);

    void inject(PushReceiver target);

    //Incident Report
    void inject(IncidentReportListActivity target);

    void inject(IncidentReportListFragment target);

    void inject(IncidentReportActivity target);

    void inject(IncidentDetailsFragment target);

    void inject(IncidentPersonDetailsFragment target);

    void inject(ReportedByDetailsFragment target);

    void inject(IncidentReportSyncService target);

    //Medication Error
    void inject(MedicationErrorListActivity target);

    void inject(MedicationErrorListFragment target);

    void inject(MedicationErrorActivity target);

    void inject(MedicationErrorDetailsFragment target);

    void inject(MedicationErrorPersonDetailsFragment target);

    void inject(ErrorReportedByDetailsFragment target);

    void inject(MedicationErrorSyncService target);

    //Adrug reaction Error
    void inject(DrugReactionListActivity target);

    void inject(DrugReactionListFragment target);

    void inject(DrugReactionActivity target);

    void inject(DrugReactionDetailsFragment target);

    void inject(DrugReactionPersonDetailsFragment target);

    void inject(DrugReactionReportedByDetailsFragment target);

    void inject(DrugReactionSyncService target);


}
