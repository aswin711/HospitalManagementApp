package com.synnefx.cqms.event.sqlite;

/**
 * Created by Josekutty on 7/14/2016.
 */
public class DataAccessException extends Exception {
    public DataAccessException() {
        super("Data access exception");
    }

    public DataAccessException(String m) {
        super(m);
    }

    public DataAccessException(String m, Throwable e) {
        super(m, e);
    }
}
