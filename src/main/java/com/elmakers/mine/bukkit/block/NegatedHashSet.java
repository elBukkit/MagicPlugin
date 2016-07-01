package com.elmakers.mine.bukkit.block;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ForwardingSet;

public class NegatedHashSet<T> extends ForwardingSet<T> {
    private static final long serialVersionUID = 1L;
    private final Set<T> delegate;

    public NegatedHashSet() {
        this(new HashSet<T>());
    }

    public NegatedHashSet(Set<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean contains(Object o) {
        return !super.contains(o);
    }

    @Override
    protected Set<T> delegate() {
        return delegate;
    }
}
