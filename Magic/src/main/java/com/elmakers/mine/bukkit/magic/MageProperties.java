package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class MageProperties extends BaseMagicConfigurable {
    private final Mage mage;

    public MageProperties(Mage mage) {
        super(MagicPropertyType.MAGE, mage.getController());
        this.mage = mage;
    }

    @Override
    protected void rebuildEffectiveConfiguration(ConfigurationSection effectiveConfiguration) {
        // Not really doing anything here, this is the root of the tree.
    }

    @Override
    public void describe(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "Properties for Mage " + ChatColor.GREEN + mage.getName());

        super.describe(sender, HIDDEN_PROPERTY_KEYS);
    }
}
