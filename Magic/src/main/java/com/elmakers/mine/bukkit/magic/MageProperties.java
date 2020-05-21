package com.elmakers.mine.bukkit.magic;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.wand.Wand;

public class MageProperties extends CasterProperties {
    private final Mage mage;
    private ColorHD effectColor = null;

    public MageProperties(Mage mage) {
        super(MagicPropertyType.MAGE, mage.getController());
        this.mage = mage;
    }

    @Override
    public void loadProperties() {
        super.loadProperties();

        if (hasProperty("effect_color")) {
            setEffectColor(getString("effect_color"));
        }
    }

    @Override
    public void updated() {
        updateMaxMana(mage);
        Wand activeWand = mage.getActiveWand();
        if (activeWand != null) {
            activeWand.updated();
        }
        mage.updatePassiveEffects();
    }

    public void setEffectColor(String hexColor) {
        // TODO: Move to CasterProperties?
        if (hexColor == null || hexColor.length() == 0 || hexColor.equals("none")) {
            effectColor = null;
            return;
        }
        effectColor = new ColorHD(hexColor);
        if (hexColor.equals("random")) {
            setProperty("effect_color", effectColor.toString());
        }
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

    @Nullable
    @Override
    public Player getPlayer() {
        return mage.getPlayer();
    }

    @Override
    public Mage getMage() {
        return mage;
    }

    @Nullable
    public Color getEffectColor() {
       return effectColor == null ? null : effectColor.getColor();
    }

    @Nullable
    @Override
    public BaseMagicConfigurable getStorage(MagicPropertyType propertyType) {
        switch (propertyType) {
            case WAND:
                return mage.getActiveWand();
            case CLASS:
            case SUBCLASS:
                return mage.getActiveClass().getStorage(propertyType);
            case MAGE:
                return this;
        }
        return null;
    }
}
