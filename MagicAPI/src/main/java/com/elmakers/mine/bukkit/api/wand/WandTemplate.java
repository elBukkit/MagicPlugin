package com.elmakers.mine.bukkit.api.wand;

import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;

public interface WandTemplate extends MagicProperties {
    String getKey();
    Collection<EffectPlayer> getEffects(String key);
    ConfigurationSection getConfiguration();
    boolean hasTag(String tag);
    String getCreatorId();
    String getCreator();
    @Nullable
    WandTemplate getMigrateTemplate();
    String migrateIcon(String icon);
    boolean isRestorable();
    Set<String> getCategories();
    ConfigurationSection getAttributes();
    String getAttributeSlot();
    @Nullable
    WandTemplate getParent();

    boolean playEffects(Wand wand, String key);
    boolean playEffects(Wand wand, String key, float scale);

    @Deprecated
    boolean playEffects(Mage mage, String key);
    @Deprecated
    boolean playEffects(Mage mage, String key, float scale);
    @Deprecated
    boolean isSoul();
}
