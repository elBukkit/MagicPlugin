package com.elmakers.mine.bukkit.api.spell;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.data.SpellData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

/**
 * This interface and its hierarchy represent the complete Spell
 * interface that must be implemented to make a new custom spell
 * class from scractch.
 * 
 * The implementations of Mage and MageController in the MagicPlugin
 * will adhere to this interface.
 * 
 * The external MagicPlugin base classes for spells are available to
 * use in MagicLib:
 * 
 * http://jenkins.elmakers.com/job/MagicLib/doxygen/
 * 
 * If you wish to create a new custom Spell from scratch, it is much
 * easier (and more consistent) to start withe the BaseSpell class or
 * one of its decendents.
 * 
 * Classes like TargetingSpell, BrushSpell, UndoableSpell all provide
 * a wide range of common functionality that will make sure your spell
 * works just like all the builtins. All of these classes are
 * available in MagicLib, and should remain relatively stable, though
 * MagicLib is not considered a strong API as such.
 */
public interface MageSpell extends Spell, CostReducer {
    void activate();
    boolean deactivate();
    boolean deactivate(boolean force, boolean quiet);
    void setActive(boolean active);

    void setMage(Mage mage);
    Mage getMage();

    void load(SpellData spellData);
    void save(SpellData spellData);
    SpellData getSpellData();
    void setSpellData(SpellData data);

    void tick();
    void initialize(MageController controller);

    @Nullable
    Double getAttribute(String attributeKey);
}
