package com.fatwire.benchmark.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class FactoryMap<T, E> {

    public interface Factory<T, E> {
        E create(T t);
    }

    private final Map<T, E> map;

    private final Factory<T, E> factory;

    /**
     * @param map
     * @param factory
     */
    public FactoryMap(final Map<T, E> map, final Factory<T, E> factory) {
        super();
        this.map = map;
        this.factory = factory;
    }

    public void clear() {
        map.clear();

    }

    /**
     * @return
     * @see java.util.Map#entrySet()
     */
    public Set<Entry<T, E>> entrySet() {
        return map.entrySet();
    }

    /**
     * @param o
     * @return
     * @see java.util.Map#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        return map.equals(o);
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#get(java.lang.Object)
     */
    public E get(T key) {

        E o = map.get(key);
        if (o == null) {
            o = factory.create(key);
            map.put(key, o);
        }
        return o;
    }

    /**
     * @return
     * @see java.util.Map#hashCode()
     */
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * @return
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * @return
     * @see java.util.Map#keySet()
     */
    public Set<T> keySet() {
        return map.keySet();
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#remove(java.lang.Object)
     */
    public E remove(Object key) {
        return map.remove(key);
    }

    /**
     * @return
     * @see java.util.Map#size()
     */
    public int size() {
        return map.size();
    }

    /**
     * @return
     * @see java.util.Map#values()
     */
    public Collection<E> values() {
        return map.values();
    }

}
