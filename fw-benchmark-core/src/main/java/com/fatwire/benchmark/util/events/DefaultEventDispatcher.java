package com.fatwire.benchmark.util.events;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class DefaultEventDispatcher<E, L extends EventListener<E>> implements
        EventDispatcher<E, L> {
    private final Set<L> listeners = new CopyOnWriteArraySet<L>();

    public void dispatch(E event) {
        if (event != null) {
            for (L listener : this.listeners) {
                listener.executePerformed(event);
            }
        }

    }

    public void addListener(L listener) {
        this.listeners.add(listener);
    }

    public void removeListener(L listener) {
        this.listeners.remove(listener);
    }

}
