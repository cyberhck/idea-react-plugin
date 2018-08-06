package com.cyberhck.react;

public interface LoggingClient {
    public static int
            DEBUG = 1,
            INFO = 2,
            NOTICE = 3,
            WARNING = 4,
            ERROR = 5,
            CRITICAL = 6,
            ALERT = 7,
            EMERGENCY = 8;

    public void debug(String message);
    public void info(String message);
    public void notice(String message);
    public void warn(String message);
    public void error(String message);
    public void critical(String message);
    public void alert(String message);
    public void emergency(String message);
}
