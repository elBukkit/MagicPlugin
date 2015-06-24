package com.elmakers.mine.bukkit.api.spell;

import org.bukkit.configuration.ConfigurationSection;

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
    public void activate();
    public boolean deactivate();
    public boolean deactivate(boolean force, boolean quiet);
    public void setActive(boolean active);

    public void setMage(Mage mage);
    public Mage getMage();

    public void load(ConfigurationSection node);
    public void save(ConfigurationSection node);

    public void tick();

    public void initialize(MageController controller);

    public void setUpgrade(MageSpell spell);
    public MageSpell getUpgrade();
}
