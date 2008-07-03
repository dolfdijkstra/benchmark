/**
 * 
 */
package com.fatwire.benchmark.util;

import java.util.Iterator;

public final class EternalIterator<T> implements Iterator<T> {
    private Iterator<T> i;

    private final Iterable<T> delegate;

    public EternalIterator(final Iterable<T> delegate) {
        this.delegate = delegate;
        i = this.delegate.iterator();
    }

    public boolean hasNext() {
        return true;
    }

    public T next() {
        if (!i.hasNext()) {
            i = delegate.iterator();
        }

        return i.next();
    }

    public void remove() {
        // TODO Auto-generated method stub

    }
}