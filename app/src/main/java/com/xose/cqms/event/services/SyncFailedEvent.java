package com.xose.cqms.audit.services;

/**
 * Created by Josekutty on 8/13/2016.
 */
public class SyncFailedEvent {
    private final boolean syncFailed;

    public SyncFailedEvent(boolean syncFailed) {
        this.syncFailed = syncFailed;
    }

    public boolean isSyncFailed() {
        return syncFailed;
    }


}
