package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class BaseMagicProperties implements MagicProperties {

    protected final @Nonnull MagicController controller;
    protected ConfigurationSection configuration = new MemoryConfiguration();
    protected boolean dirty = false;

    protected BaseMagicProperties(MageController controller) {
        // Don't really like this, but Wand is very dependent on MagicController
        Preconditions.checkArgument(controller instanceof MagicController);
        this.controller = (MagicController)controller;
    }

    public void load(ConfigurationSection configuration) {
        this.configuration = ConfigurationUtils.cloneConfiguration(configuration);
        dirty = true;
    }

    public boolean hasOwnProperty(String key) {
        return configuration.contains(key);
    }

    @Override
    public boolean hasProperty(String key) {
        return getEffectiveConfiguration().contains(key);
    }

    @Override
    public Object getProperty(String key) {
        return getEffectiveConfiguration().get(key);
    }

    @Override
    public <T> Optional<T> getProperty(String key, Class<T> type) {
        Object value = getEffectiveConfiguration().get(key);
        if(value == null || !type.isInstance(value)) {
            return Optional.absent();
        }

        return Optional.of(type.cast(value));
    }

    @Override
    public <T> T getProperty(String key, T defaultValue) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(defaultValue, "defaultValue");

        @SuppressWarnings("unchecked")
        Class<? extends T> clazz = (Class<? extends T>) defaultValue.getClass();

        Object value = getEffectiveConfiguration().get(key);
        if (value != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        return defaultValue;
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    public ConfigurationSection getEffectiveConfiguration() {
        return configuration;
    }
    
    public void clear() {
        configuration = new MemoryConfiguration();
        dirty = false;
    }

    protected static String getPotionEffectString(Map<PotionEffectType, Integer> potionEffects) {
        if (potionEffects.size() == 0) return null;
        Collection<String> effectStrings = new ArrayList<>();
        for (Map.Entry<PotionEffectType, Integer> entry : potionEffects.entrySet()) {
            String effectString = entry.getKey().getName();
            if (entry.getValue() > 0) {
                effectString += ":" + entry.getValue();
            }
            effectStrings.add(effectString);
        }
        return StringUtils.join(effectStrings, ",");
    }

    protected String describePotionEffect(PotionEffectType effect, int level) {
        String effectName = effect.getName();
        String effectFirst = effectName.substring(0, 1);
        effectName = effectName.substring(1).toLowerCase().replace("_", " ");
        effectName = effectFirst + effectName;
        return controller.getMessages().getLevelString("wand.potion_effect", level, 5).replace("$effect", effectName);
    }

    protected void sendDebug(String debugMessage) {
        // Does nothing unless overridden
    }

    protected void sendMessage(String messageKey) {
        // Does nothing unless overridden
    }

    protected void sendAddMessage(String messageKey, String nameParam) {
        String message = getMessage(messageKey).replace("$name", nameParam);
        sendMessage(message);
    }

    protected String getMessage(String messageKey) {
        return getMessage(messageKey, "");
    }

    protected String getMessage(String messageKey, String defaultValue) {
        return controller.getMessages().get(messageKey, defaultValue);
    }
}
