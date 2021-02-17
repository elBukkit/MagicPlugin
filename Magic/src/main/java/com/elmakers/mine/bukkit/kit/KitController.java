package com.elmakers.mine.bukkit.kit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.configuration.MagicConfiguration;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class KitController {
    private final MagicController controller;
    private final Map<String, MagicKit> kits = new HashMap<>();
    private final List<MagicKit> joinKits = new ArrayList<>();

    public KitController(MagicController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection configuration) {
        configuration = new MagicConfiguration(controller, configuration);
        kits.clear();
        joinKits.clear();
        Set<String> keys = configuration.getKeys(false);
        for (String key : keys) {
            ConfigurationSection kitConfiguration = configuration.getConfigurationSection(key);
            if (!ConfigurationUtils.isEnabled(kitConfiguration)) continue;
            MagicKit kit = new MagicKit(controller, key, kitConfiguration);
            kits.put(key, kit);
            if (kit.isKeep() || kit.isRemove() || kit.isStarter()) {
                joinKits.add(kit);
            }
        }
    }

    public int getCount() {
        return kits.size();
    }

    public void onJoin(Mage mage) {
        for (MagicKit joinKit : joinKits) {
            if (joinKit.isStarter()) {
                joinKit.checkGive(mage);
            }
            if (joinKit.isRemove()) {
                joinKit.checkRemoveFrom(mage);
            }
            if (joinKit.isKeep()) {
                joinKit.giveMissing(mage);
            }
        }
    }

    public MagicKit getKit(String key) {
        return kits.get(key);
    }

    public Collection<String> getKitKeys() {
        return kits.keySet();
    }
}
