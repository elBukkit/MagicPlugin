package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class MobTrigger extends CustomTrigger {
    public MobTrigger(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        super(controller, key, configuration);
    }
}
