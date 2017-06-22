package com.synnefx.cqms.audit.services;

/**
 * Created by Josekutty on 8/13/2016.
 */
public class SyncStartedEvent {

    private final boolean syncStarted;

    public SyncStartedEvent(boolean syncStarted) {
        this.syncStarted = syncStarted;
    }

    public boolean isSyncStarted() {
        return syncStarted;
    }
}
