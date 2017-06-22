package com.synnefx.cqms.event.events;

import com.synnefx.cqms.event.ui.base.BootstrapActivity;

/**
 * The event that is posted when a network error event occurs.
 * TODO: Consume this event in the {@link BootstrapActivity} and
 * show a dialog that something went wrong.
 */
public class NetworkErrorEvent {
    private Exception cause;

    public NetworkErrorEvent(Exception cause) {
        this.cause = cause;
    }

    public Exception getCause() {
        return cause;
    }
}
