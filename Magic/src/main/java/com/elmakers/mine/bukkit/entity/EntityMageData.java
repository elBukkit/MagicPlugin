package com.elmakers.mine.bukkit.entity;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;

public class EntityMageData {
    // These properties will get copied directly to mage data, as if they were in the "mage" section.
    private static final String[] MAGE_PROPERTIES = {"protection", "weakness", "strength"};

    protected long tickInterval;
    protected LinkedList<WeightedPair<String>> spells;
    protected LinkedList<WeightedPair<String>> deathSpells;
    protected ConfigurationSection mageProperties;
    protected boolean requiresTarget;
    protected ItemData requiresWand;
    protected boolean aggro;

    public EntityMageData(@Nonnull MageController controller, @Nonnull ConfigurationSection parameters) {
        requiresWand = controller.getOrCreateItem(parameters.getString("cast_requires_item"));

        mageProperties = parameters.getConfigurationSection("mage");

        for (String mageProperty : MAGE_PROPERTIES) {
            ConfigurationSection mageConfig = parameters.getConfigurationSection(mageProperty);
            if (mageConfig != null) {
                if (mageProperties == null) {
                    mageProperties = new MemoryConfiguration();
                }
                mageProperties.set(mageProperty, mageConfig);
            }
        }

        tickInterval = parameters.getLong("cast_interval", 0);
        if (parameters.contains("cast")) {
            spells = new LinkedList<>();
            RandomUtils.populateStringProbabilityMap(spells, parameters.getConfigurationSection("cast"));
        }
        if (parameters.contains("death_cast")) {
            deathSpells = new LinkedList<>();
            RandomUtils.populateStringProbabilityMap(deathSpells, parameters.getConfigurationSection("death_cast"));
        }
        requiresTarget = parameters.getBoolean("cast_requires_target", true);
        aggro = parameters.getBoolean("aggro", !isEmpty());
    }

    public boolean isEmpty() {
        boolean hasSpells = spells != null && tickInterval >= 0;
        hasSpells = hasSpells || deathSpells != null;
        boolean hasProperties = mageProperties != null;
        return !hasProperties && !hasSpells && !aggro;
    }

    private void cast(Mage mage, String castSpell) {
        if (castSpell.length() > 0) {
            String[] parameters = null;
            Spell spell = null;
            if (!castSpell.equalsIgnoreCase("none"))
            {
                if (castSpell.contains(" ")) {
                    parameters = StringUtils.split(castSpell, ' ');
                    castSpell = parameters[0];
                    parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
                }
                spell = mage.getSpell(castSpell);
            }
            if (spell != null) {
                spell.cast(parameters);
            }
        }
    }

    public void onDeath(Mage mage) {
        if (deathSpells == null || deathSpells.isEmpty()) return;

        String deathSpell = RandomUtils.weightedRandom(deathSpells);
        cast(mage, deathSpell);
    }

    public void tick(Mage mage) {
        if (spells == null || spells.isEmpty()) return;
        Entity entity = mage.getLivingEntity();
        Creature creature = (entity instanceof Creature) ? (Creature)entity : null;
        if (requiresTarget && (creature == null || creature.getTarget() == null)) return;
        if (requiresWand != null && entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            ItemStack itemInHand = li.getEquipment().getItemInMainHand();
            if (itemInHand == null || itemInHand.getType() != requiresWand.getType()) return;
        }

        String castSpell = RandomUtils.weightedRandom(spells);
        cast(mage, castSpell);
    }
}
