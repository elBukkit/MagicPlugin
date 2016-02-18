package com.elmakers.mine.bukkit.spell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.elmakers.mine.bukkit.api.magic.Messages;
import org.bukkit.Color;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class SpellCategory implements com.elmakers.mine.bukkit.api.spell.SpellCategory {
    protected List<SpellTemplate> spells = new ArrayList<SpellTemplate>();
    protected final String key;
    protected final String name;
    protected final String description;
    protected final Color color;
    protected long castCount = 0;
    protected long lastCast = 0;
    protected final MageController controller;

    public SpellCategory(String key, MageController controller) {
        this.key = key;
        this.controller = controller;
        Messages messages = controller.getMessages();
        name = messages.get("categories." + key + ".name", key);
        description = messages.get("categories." + key + ".description", "");
        color = ConfigurationUtils.toColor(messages.get("categories." + key + ".color", ""));
    }

    public SpellCategory(String key, MageController controller, long castCount, long lastCast) {
        this(key, controller);
        this.castCount = castCount;
        this.lastCast = lastCast;
    }

    public void addSpellTemplate(SpellTemplate template)
    {
        spells.add(template);
    }

    @Override
    public Collection<SpellTemplate> getSpells()
    {
        return spells;
    }

    /* (non-Javadoc)
     * @see com.elmakers.mine.bukkit.spell.SpellCategoryInterface#addCast()
     */
    @Override
    public void addCast() {
        castCount++;
        lastCast = System.currentTimeMillis();
    }

    /* (non-Javadoc)
     * @see com.elmakers.mine.bukkit.spell.SpellCategoryInterface#addCasts()
     */
    @Override
    public void addCasts(long castCount, long lastCast) {
        this.castCount += castCount;
        this.lastCast = Math.max(this.lastCast, lastCast);
    }

    /* (non-Javadoc)
     * @see com.elmakers.mine.bukkit.spell.SpellCategoryInterface#getCastCount()
     */
    @Override
    public long getCastCount() {
        return castCount;
    }

    /* (non-Javadoc)
     * @see com.elmakers.mine.bukkit.spell.SpellCategoryInterface#getLastCast()
     */
    @Override
    public long getLastCast() {
        return lastCast;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public int compareTo(com.elmakers.mine.bukkit.api.spell.SpellCategory other)
    {
        return name.compareTo(other.getName());
    }
}
