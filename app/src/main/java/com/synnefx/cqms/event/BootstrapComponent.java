package com.synnefx.cqms.event;

import android.preference.PreferenceActivity;

import com.synnefx.cqms.event.authenticator.BootstrapAuthenticatorActivity;
import com.synnefx.cqms.event.core.pushy.PushReceiver;
import com.synnefx.cqms.event.sync.conf.ConfSyncService;
import com.synnefx.cqms.event.sync.drugreaction.DrugReactionSyncService;
import com.synnefx.cqms.event.sync.incident.IncidentReportSyncService;
import com.synnefx.cqms.event.sync.medicationerror.MedicationErrorSyncService;
import com.synnefx.cqms.event.ui.ImportConfigActivity;
import com.synnefx.cqms.event.ui.MainActivity;
import com.synnefx.cqms.event.ui.SpashscreenActivity;
import com.synnefx.cqms.event.ui.base.BootstrapActivity;
import com.synnefx.cqms.event.ui.base.BootstrapFragment;
import com.synnefx.cqms.event.ui.base.BootstrapFragmentActivity;
import com.synnefx.cqms.event.ui.base.NavigationDrawerFragment;
import com.synnefx.cqms.event.ui.drugreaction.DrugInfoFragment;
import com.synnefx.cqms.event.ui.drugreaction.DrugReactionActivity;
import com.synnefx.cqms.event.ui.drugreaction.DrugReactionDetailsFragment;
import com.synnefx.cqms.event.ui.drugreaction.DrugReactionDiagnosisDetailsFragment;
import com.synnefx.cqms.event.ui.drugreaction.DrugReactionListActivity;
import com.synnefx.cqms.event.ui.drugreaction.DrugReactionListFragment;
import com.synnefx.cqms.event.ui.drugreaction.DrugReactionReportedByDetailsFragment;
import com.synnefx.cqms.event.ui.drugreaction.PatientDetailsFragment;
import com.synnefx.cqms.event.ui.incident.IncidentDetailsFragment;
import com.synnefx.cqms.event.ui.incident.IncidentPersonDetailsFragment;
import com.synnefx.cqms.event.ui.incident.IncidentReportActivity;
import com.synnefx.cqms.event.ui.incident.IncidentReportListActivity;
import com.synnefx.cqms.event.ui.incident.IncidentReportListFragment;
import com.synnefx.cqms.event.ui.incident.IncidentReportViewActivity;
import com.synnefx.cqms.event.ui.incident.ReportedByDetailsFragment;
import com.synnefx.cqms.event.ui.medicationerror.ErrorReportedByDetailsFragment;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorActivity;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorDetailsFragment;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorListActivity;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorListFragment;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorPersonDetailsFragment;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorViewActivity;

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

    void inject(IncidentReportViewActivity target);

    //Medication Error
    void inject(MedicationErrorListActivity target);

    void inject(MedicationErrorListFragment target);

    void inject(MedicationErrorActivity target);

    void inject(MedicationErrorDetailsFragment target);

    void inject(MedicationErrorPersonDetailsFragment target);

    void inject(ErrorReportedByDetailsFragment target);

    void inject(MedicationErrorSyncService target);

    void inject(MedicationErrorViewActivity target);

    //Adrug reaction Error
    void inject(DrugReactionListActivity target);

    void inject(DrugReactionListFragment target);

    void inject(DrugReactionActivity target);

    void inject(DrugReactionDetailsFragment target);

    void inject(PatientDetailsFragment target);

    void inject(DrugReactionDiagnosisDetailsFragment target);

    void inject(DrugInfoFragment target);

    void inject(DrugReactionReportedByDetailsFragment target);

    void inject(DrugReactionSyncService target);


    void inject(BootstrapFragment bootstrapFragment);
}
