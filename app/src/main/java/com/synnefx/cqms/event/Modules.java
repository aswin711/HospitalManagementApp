package com.synnefx.cqms.event;


final class Modules {
    static Object[] list() {
        return new Object[]{
                new AndroidModule(),
                new BootstrapModule()
        };
    }

    private Modules() {
        // No instances.
    }
}
