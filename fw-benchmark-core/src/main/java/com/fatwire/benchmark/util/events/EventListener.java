package com.fatwire.benchmark.util.events;

public interface EventListener<E> {

    void executePerformed(E event);
}
