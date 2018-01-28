package com.javieu.crypto.binance;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class Log {

    private static final Map<String, Logger> LOGGERS = new TreeMap<String, Logger>();

    public static void severe(Class instanceClass, String message, Exception exception) {
        getLogger(instanceClass).error(message, exception);
    }

    public static void severe(Class instanceClass, String message) {
        getLogger(instanceClass).error(message);
    }

    public static void info(Class instanceClass, String message) {
        getLogger(instanceClass).info(message);
    }

    public static void info(Class instanceClass, String message, Exception ex) {
        getLogger(instanceClass).info(message, ex);
    }

    public static void debug(Class instanceClass, String message) {
        getLogger(instanceClass).debug(message);
    }

    public static void warning(Class instanceClass, String message) {
        getLogger(instanceClass).warn(message);
    }

    public static void warning(Class instanceClass, String message, Exception ex) {
        getLogger(instanceClass).warn(message, ex);
    }

    public static void fine(Class instanceClass, String message) {
        getLogger(instanceClass).trace(message);
    }

    private static Logger getLogger(Class instanceClass) {
        Logger logger = LOGGERS.get(instanceClass.getName());
        if (logger == null) {
            logger = LoggerFactory.getLogger(instanceClass);
            LOGGERS.put(instanceClass.getName(), logger);
        }
        return logger;
    }
}
