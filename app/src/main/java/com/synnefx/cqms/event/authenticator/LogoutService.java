package com.synnefx.cqms.event.authenticator;

public interface LogoutService {
    void logout(Runnable onSuccess);
}
