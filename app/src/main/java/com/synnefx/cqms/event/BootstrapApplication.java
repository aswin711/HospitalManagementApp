package com.synnefx.cqms.event;

import android.app.Application;
import android.os.Build;

import com.beardedhen.androidbootstrap.*;
import com.facebook.stetho.*;
import com.facebook.stetho.BuildConfig;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.rhino.JsRuntimeReplFactoryBuilder;



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

        Stetho.initializeWithDefaults(this);

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableWebKitInspector(new InspectorModulesProvider() {
                    @Override
                    public Iterable<ChromeDevtoolsDomain> get() {
                        return new Stetho.DefaultInspectorModulesBuilder(getApplicationContext()).runtimeRepl(
                                new JsRuntimeReplFactoryBuilder(getApplicationContext())
                                        .addVariable("foo", "bar")
                                        .build()
                        ).finish();
                    }
                })
                .build());


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
