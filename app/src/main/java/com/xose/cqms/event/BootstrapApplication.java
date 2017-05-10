package com.xose.cqms.event;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;


/**
 * CQMS application
 */
public abstract class BootstrapApplication extends Application {

    public static final String PACKAGE_NAME = BootstrapApplication.class.getPackage().getName();

    private static BootstrapApplication instance;
    private BootstrapComponent component;

    /**
     * Create main application
     */
    public BootstrapApplication() {
    }


    @Override
    public void onCreate() {
        super.onCreate();

        init();

        instance = this;

        // Perform injection
        //Injector.init(this, )
        component = DaggerComponentInitializer.init();
        TypefaceProvider.registerDefaultIconSets();
        onAfterInjection();
    }

    public static BootstrapComponent component() {
        return instance.component;
    }

    protected abstract void onAfterInjection();

    protected abstract void init();

    public static BootstrapApplication getInstance() {
        return instance;
    }

    public BootstrapComponent getComponent() {
        return component;
    }

    public final static class DaggerComponentInitializer {

        public static BootstrapComponent init() {
            return DaggerBootstrapComponent.builder()
                    .androidModule(new AndroidModule())
                    .bootstrapModule(new BootstrapModule())
                    .build();
        }

    }
}
