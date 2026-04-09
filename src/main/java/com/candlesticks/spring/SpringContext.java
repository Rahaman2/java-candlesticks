package com.candlesticks.spring;

import org.springframework.context.ApplicationContext;

/**
 * Static holder for the Spring {@link ApplicationContext}.
 * Populated by {@link SpringCandleApplication} before JavaFX launches,
 * then read by {@link SpringChartApp} to retrieve beans.
 */
public class SpringContext {

    private static ApplicationContext context;

    public static void set(ApplicationContext ctx) {
        context = ctx;
    }

    public static ApplicationContext get() {
        return context;
    }

    public static <T> T getBean(Class<T> type) {
        return context.getBean(type);
    }
}
