package com.roboo.mineshafttycoonutils.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.AbstractFilter;

import java.security.SignatureException;

public class LogNoiseFilter extends AbstractFilter {

    private static final String AUTHLIB_PREFIX = "com.mojang.authlib";
    private static final String[] NOISY_MESSAGES = {
            "Failed to verify signature on property",
            "Profile contained invalid signature"
    };

    private LogNoiseFilter() {}

    public static void install() {
        try {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();
            LogNoiseFilter filter = new LogNoiseFilter();

            config.getAppenders().values().forEach(appender -> {
                if (appender instanceof org.apache.logging.log4j.core.filter.Filterable filterable) {
                    filterable.addFilter(filter);
                }
            });

            config.getRootLogger().addFilter(filter);
            context.updateLoggers();
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    public Result filter(LogEvent event) {
        return isNoise(event) ? Result.DENY : Result.NEUTRAL;
    }

    private static boolean isNoise(LogEvent event) {
        String logger = event.getLoggerName();
        if (logger != null && logger.startsWith(AUTHLIB_PREFIX)
                && event.getThrown() instanceof SignatureException) {
            return true;
        }
        if (event.getMessage() == null) {
            return false;
        }
        String message = event.getMessage().getFormattedMessage();
        if (message == null) {
            return false;
        }
        for (String noisy : NOISY_MESSAGES) {
            if (message.startsWith(noisy)) {
                return true;
            }
        }
        return false;
    }
}