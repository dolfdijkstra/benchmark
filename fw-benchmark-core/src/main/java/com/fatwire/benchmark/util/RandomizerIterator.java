/**
 * 
 */
package com.fatwire.benchmark.util;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class RandomizerIterator<T> implements Iterator<T> {

    private final List<T> delegate;
    
    Random r = new Random();

    public RandomizerIterator(final List<T> delegate) {
        this.delegate = delegate;
    }

    public boolean hasNext() {
        return true;
    }

    public T next() {

        return delegate.get(random());
    }

    private int random() {
        return (int)(Math.random()* delegate.size());
    }

    public void remove() {
        // TODO Auto-generated method stub

    }
}