package com.xose.cqms.event;


import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class BootstrapApplicationImpl extends BootstrapApplication {

    @Override
    protected void onAfterInjection() {
    }

    @Override
    protected void init() {
        CrashlyticsCore core = new CrashlyticsCore.Builder()
                //.disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsTree());
        }
        //Timber.plant(new Timber.DebugTree());
    }
}
