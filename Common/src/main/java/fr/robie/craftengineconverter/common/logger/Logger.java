package fr.robie.craftengineconverter.common.logger;

import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.format.TextFormatter;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger extends TextFormatter {
    private final String prefix;
    private static Logger logger;

    public Logger(String prefix) {
        this.prefix = prefix;
        logger = this;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void info(String message, LogType type) {
        getLogger().log(message, type);
    }

    public static void info(String message) {
        getLogger().log(message, LogType.INFO);
    }

    public static void info(Message message) {
        getLogger().log(message, LogType.INFO);
    }

    public static void info(String message, LogType type, Object... args) {
        getLogger().log(message, type, args);
    }

    public static void info(Message message, LogType type, Object... args) {
        getLogger().log(message, type, args);
    }

    public static void info(Message message, Object... args) {
        getLogger().log(message, LogType.INFO, args);
    }

    public static void debug(String message) {
        getLogger().logDebug(message, LogType.WARNING);
    }

    public static void debug(String message, LogType type) {
        getLogger().logDebug(message, type);
    }

    public static void debug(String message, LogType type, Object... args) {
        getLogger().logDebug(message, type, args);
    }

    public static void debug(String message, Object... args) {
        getLogger().logDebug(message, LogType.WARNING, args);
    }

    public static void debug(Message message, Object... args) {
        getLogger().logDebug(message.getMessage(), LogType.WARNING, args);
    }

    public static void debug(Message message, LogType type, Object... args) {
        getLogger().logDebug(message.getMessage(), type, args);
    }

    public static void showException(String errorName,Throwable throwable) {
        getLogger().logException(errorName, throwable);
    }

    public String getPrefix() {
        return prefix;
    }

    public void log(String message, LogType logType, Object... args){
        Bukkit.getConsoleSender().sendMessage("§8[§e" + prefix + "§8] " + logType.getColor() + parseText(message, args));
    }

    public void log(Message message, LogType logType, Object... args){
        this.log(message.getMessage(), logType, args);
    }

    public void logDebug(String message, LogType type, Object... args) {
        if (Configuration.enableDebug){
            log(message, type, args);
        }
    }

    public void logException(String errorName, Throwable throwable){
        if (!Configuration.enableDebug) return;
        this.log("An exception occurred while " + errorName + ":", LogType.ERROR);
        this.log("Exception error message: " + throwable.getMessage(), LogType.ERROR);
        this.log("Please check the stack trace below for more details. If you don't understand the issue report it to the developer.",LogType.ERROR);
        this.log("------------------- Stack Trace ------------------",LogType.ERROR);
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }
        this.log(sw.toString(), LogType.ERROR);
        this.log("--------------------------------------------------",LogType.ERROR);
    }

    public String getColoredMessage(String message) {
        return message.replace("<&>", "§");
    }
}