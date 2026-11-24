package com.oes.exam;

/** Source of the current time in seconds. Injected so that tests can control time. */
@FunctionalInterface
public interface TimeSource {

    long nowSeconds();

    TimeSource SYSTEM = () -> System.currentTimeMillis() / 1000;
}
