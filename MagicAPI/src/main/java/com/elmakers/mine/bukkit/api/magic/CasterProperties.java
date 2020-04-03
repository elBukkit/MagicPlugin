package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import javax.annotation.Nullable;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

public interface CasterProperties extends MagicConfigurable {
    boolean hasSpell(String spellKey);
    Collection<String> getSpells();
    boolean addSpell(String spellKey);
    boolean setSpelLLevel(String spellKey, int level);
    Mage getMage();
    void removeMana(float mana);
    float getMana();
    int getManaMax();
    int getEffectiveManaMax();
    void setMana(float mana);
    void setManaMax(int manaMax);
    int getManaRegeneration();
    int getEffectiveManaRegeneration();
    void setManaRegeneration(int manaRegeneration);
    @Nullable
    ProgressionPath getPath();
    boolean canProgress();
    @Nullable
    Double getAttribute(String attributeKey);
    void setAttribute(String attributeKey, Double attributeValue);
    boolean addItem(ItemStack item);

    /**
     * Returns a Spell for a given spell key, if this caster has the spell.
     *
     * <p>Will return the correct spell level for the given spell, regardless of the level requested.
     *
     * @param spellKey The spell key to request
     * @return the Spell, or null if not present here.
     */
    @Nullable
    Spell getSpell(String spellKey);

    /**
     * Returns the SpellTemplate representing the requested spell key at the correct level.
     *
     * @param spellKey The key of the spell being requested
     * @return The SpellTemplate requested, or null if this class doesn't have the requested spell
     */
    @Nullable
    SpellTemplate getSpellTemplate(String spellKey);
}
