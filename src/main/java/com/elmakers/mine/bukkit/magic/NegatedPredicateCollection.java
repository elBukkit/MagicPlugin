package com.elmakers.mine.bukkit.magic;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.elmakers.mine.bukkit.api.magic.MaterialPredicate;
import com.google.common.base.Function;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.Iterators;

public class NegatedPredicateCollection
        extends ForwardingCollection<MaterialPredicate> {
    private final Collection<MaterialPredicate> delegate;

    public NegatedPredicateCollection(
            Collection<MaterialPredicate> delegate) {
        this.delegate = Collections.unmodifiableCollection(delegate);
    }

    @Override
    public Iterator<MaterialPredicate> iterator() {
        return Iterators.transform(super.iterator(),
                new Function<MaterialPredicate, MaterialPredicate>() {
                    @Override
                    public MaterialPredicate apply(
                            MaterialPredicate predicate) {
                        return NegatedMaterialPredicate.of(predicate);
                    }
                });
    }

    @Override
    protected Collection<MaterialPredicate> delegate() {
        return delegate;
    }
}
