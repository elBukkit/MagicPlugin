package com.elmakers.mine.bukkit.magic;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MageProperties extends CasterProperties {
    private final Mage mage;

    public MageProperties(Mage mage) {
        super(MagicPropertyType.MAGE, mage.getController());
        this.mage = mage;
    }

    @Override
    public void describe(CommandSender sender, @Nullable Set<String> ignoreProperties, @Nullable Set<String> overriddenProperties) {
        MageClass activeClass = mage.getActiveClass();
        if (activeClass != null) {
            if (overriddenProperties == null) {
                overriddenProperties = new HashSet<>();
            }
            overriddenProperties.addAll(activeClass.getEffectiveConfiguration(false).getKeys(false));
        }

        super.describe(sender, ignoreProperties, overriddenProperties);
    }

    @Override
    public boolean isPlayer() {
        return mage.isPlayer();
    }

    @Override
    public Player getPlayer() {
        return mage.getPlayer();
    }

    @Override
    public Mage getMage() {
        return mage;
    }
}
