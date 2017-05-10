package com.xose.cqms.event.authenticator;

public interface LogoutService {
    void logout(Runnable onSuccess);
}
