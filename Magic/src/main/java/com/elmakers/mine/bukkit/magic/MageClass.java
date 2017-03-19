package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nonnull;

public class MageClass extends MageClassProperties {
    public MageClass(@Nonnull Mage mage, @Nonnull MageClassTemplate template) {
        super(mage.getProperties(), template, mage.getController());
    }

    public MageClassTemplate getTemplate() {
        return template;
    }
}
