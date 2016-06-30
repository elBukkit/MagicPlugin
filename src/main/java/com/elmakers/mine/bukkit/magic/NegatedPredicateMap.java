package com.elmakers.mine.bukkit.magic;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.api.magic.MaterialPredicate;
import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class NegatedPredicateMap
        extends ForwardingMultimap<Material, MaterialPredicate> {
    private final Multimap<Material, MaterialPredicate> delegate = HashMultimap
            .create();

    @Override
    public Collection<MaterialPredicate> get(Material mat) {
        Collection<MaterialPredicate> predicates = super.get(mat);

        if (predicates.size() == 0) {
            return Collections.singletonList(MaterialPredicate.TRUE);
        }

        return new NegatedPredicateCollection(predicates);
    }

    @Override
    protected Multimap<Material, MaterialPredicate> delegate() {
        return delegate;
    }
}
