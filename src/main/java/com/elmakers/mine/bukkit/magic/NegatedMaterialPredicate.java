package com.elmakers.mine.bukkit.magic;

import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MaterialPredicate;

public class NegatedMaterialPredicate implements MaterialPredicate {
    private final MaterialPredicate delegate;

    private NegatedMaterialPredicate(MaterialPredicate delegate) {
        this.delegate = delegate;
    }

    public static MaterialPredicate of(MaterialPredicate delegate) {
        if (delegate == MaterialPredicate.TRUE) {
            return MaterialPredicate.FALSE;
        } else if (delegate == MaterialPredicate.FALSE) {
            return MaterialPredicate.TRUE;
        } else {
            return new NegatedMaterialPredicate(delegate);
        }
    }

    @Override
    public boolean apply(BlockState block) {
        return !delegate.apply(block);
    }

    @Override
    public boolean apply(ItemStack is) {
        return !delegate.apply(is);
    }
}
