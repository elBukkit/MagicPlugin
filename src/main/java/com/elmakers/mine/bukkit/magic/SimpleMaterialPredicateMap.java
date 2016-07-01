package com.elmakers.mine.bukkit.magic;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MaterialPredicate;
import com.elmakers.mine.bukkit.api.magic.MaterialPredicateMap;
import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class SimpleMaterialPredicateMap
        extends ForwardingMultimap<Material, MaterialPredicate>
        implements MaterialPredicateMap {
    private Multimap<Material, MaterialPredicate> delegate = HashMultimap
            .create();

    public SimpleMaterialPredicateMap() {
    }

    public SimpleMaterialPredicateMap(MaterialPredicateMap other) {
        delegate.putAll(((SimpleMaterialPredicateMap) other).delegate);
    }

    @Override
    public Set<Material> getLegacyMaterials() {
        return keySet();
    }

    @Override
    public boolean apply(BlockState state) {
        for (MaterialPredicate predicate : get(state.getType())) {
            if (predicate.apply(state)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean apply(ItemStack is) {
        for (MaterialPredicate predicate : get(is.getType())) {
            if (predicate.apply(is)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected Multimap<Material, MaterialPredicate> delegate() {
        return delegate;
    }
}
