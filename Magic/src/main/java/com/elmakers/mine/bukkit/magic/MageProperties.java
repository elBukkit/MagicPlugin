package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import org.bukkit.configuration.ConfigurationSection;

public class MageProperties extends BaseMagicConfigurable {
    public MageProperties(Mage mage) {
        super(MagicPropertyType.MAGE, mage.getController());
    }

    @Override
    protected void addEffectiveConfiguration(ConfigurationSection effectiveConfiguration) {
        // Not really doing anything here, this is the root of the tree.
    }
}
