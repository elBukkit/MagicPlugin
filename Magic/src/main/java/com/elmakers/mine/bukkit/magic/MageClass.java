package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nonnull;

public class MageClass extends MageClassProperties {
    public MageClass(@Nonnull Mage mage) {
        super(mage.getProperties(), mage.getController());
    }
}
