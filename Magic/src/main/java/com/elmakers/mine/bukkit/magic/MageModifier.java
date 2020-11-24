package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.collect.ImmutableSet;

public class MageModifier extends BaseMageModifier implements Comparable<MageModifier>, com.elmakers.mine.bukkit.api.magic.MageModifier {
    public static final ImmutableSet<String> PROPERTY_KEYS = ImmutableSet.of("items", "duration");

    public MageModifier(@Nonnull Mage mage, @Nonnull ModifierTemplate template) {
        super(mage, MagicPropertyType.MODIFIER, template);
        this.setTemplate(template);
    }

    @Override
    protected String getMessageKey(String key) {
        TemplateProperties template = getTemplate();
        if (template != null) {
            String mageKey = "modifiers." + template.getKey() + "." + key;
            if (controller.getMessages().containsKey(mageKey)) {
                return mageKey;
            }
        }
        return "modifier." + key;
    }

    public void onRemoved() {
        deactivate();
        takeItems();
        cancelTrigger("added");
        trigger("removed");
    }

    public void onAdd(int duration) {
        setProperty("last_add", System.currentTimeMillis());
        activate();
        giveItems("items");
        reset(duration);
        trigger("added");
    }

    public void setTemplate(@Nonnull ModifierTemplate template) {
        super.setTemplate(template.getMageTemplate(getMage()));
    }

    @Override
    public void load(@Nullable ConfigurationSection configuration) {
        this.configuration = new MageParameters(getMage(), "Mage modifier " + getKey());
        if (configuration != null) {
            ConfigurationUtils.addConfigurations(this.configuration, configuration);
        }
        this.loadProperties();
    }

    public void reset(int duration) {
        setProperty("duration", duration);
        setProperty("last_reset", System.currentTimeMillis());
        trigger("reset");
    }

    @Override
    public boolean hasDuration() {
        return getDuration() > 0;
    }

    @Override
    public int getDuration() {
        return getInt("duration");
    }

    @Override
    public int getTimeRemaining() {
        long now = System.currentTimeMillis();
        return (int)(getLong("last_reset") + (long)getDuration() - now);
    }

    @Override
    public int compareTo(MageModifier other) {
        boolean has = hasDuration();
        boolean otherHas = other.hasDuration();
        if (has && !otherHas) return 1;
        if (otherHas && !has) return -1;
        return getTimeRemaining() - other.getTimeRemaining();
    }

    @Override
    @Nullable
    public ModifierTemplate getTemplate() {
        return (ModifierTemplate)super.getTemplate();
    }

    @Override
    @Nonnull
    public String getName() {
        ModifierTemplate template = getTemplate();
        return template == null ? "?" : template.getName();
    }

    @Override
    @Nullable
    public String getDescription() {
        return getTemplate().getDescription();
    }
}
