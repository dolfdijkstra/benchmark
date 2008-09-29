package com.fatwire.benchmark.util.events;

public interface EventDispatcher<E,L extends EventListener<E>> {
    public void dispatch(E event);

    public void addListener(L listener);

    public void removeListener(L listener);
}
