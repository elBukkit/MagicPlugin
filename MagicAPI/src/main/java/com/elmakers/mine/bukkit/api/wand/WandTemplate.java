package com.elmakers.mine.bukkit.api.wand;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.Set;

public interface WandTemplate extends MagicProperties {
    public String getKey();
    public Collection<EffectPlayer> getEffects(String key);
    public boolean hasTag(String tag);
    public String getCreatorId();
    public String getCreator();
    public boolean playEffects(Mage mage, String key);
    public boolean playEffects(Mage mage, String key, float scale);
    public WandTemplate getMigrateTemplate();
    public String migrateIcon(String icon);
    public boolean isSoul();
    public boolean isRestorable();
    Set<String> getCategories();
    ConfigurationSection getAttributes();
    String getAttributeSlot();
}
