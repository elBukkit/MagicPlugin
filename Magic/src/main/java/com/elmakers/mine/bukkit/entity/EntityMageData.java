package com.elmakers.mine.bukkit.entity;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class EntityMageData {
    // These properties will get copied directly to mage data, as if they were in the "mage" section.
    private static final String[] MAGE_PROPERTIES = {"protection", "weakness", "strength"};

    protected long tickInterval;
    protected LinkedList<WeightedPair<String>> spells;
    protected LinkedList<WeightedPair<String>> deathSpells;
    protected List<String> deathCommands;
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
        deathCommands = ConfigurationUtils.getStringList(parameters, "death_commands");
        requiresTarget = parameters.getBoolean("cast_requires_target", true);
        aggro = parameters.getBoolean("aggro", !isEmpty());
    }

    public boolean isEmpty() {
        boolean hasSpells = spells != null && tickInterval >= 0;
        hasSpells = hasSpells || deathSpells != null;
        boolean hasProperties = mageProperties != null;
        return !hasProperties && !hasSpells && !aggro && deathCommands == null;
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
        if (deathSpells != null && !deathSpells.isEmpty()) {
            String deathSpell = RandomUtils.weightedRandom(deathSpells);
            cast(mage, deathSpell);
        }
        if (deathCommands != null) {
            Entity topDamager = mage.getTopDamager();
            Entity killer = mage.getLastDamager();
            Collection<Entity> damagers = mage.getDamagers();
            Location location = mage.getLocation();
            for (String command : deathCommands) {
                if (command.contains("@killer")) {
                    if (killer == null) continue;
                    command.replace("@killer", killer.getName());
                }
                if (command.contains("@damager")) {
                    if (topDamager == null) continue;
                    command.replace("@damager", topDamager.getName());
                }

                boolean allDamagers = command.contains("@damagers");
                if (allDamagers && damagers == null) {
                    continue;
                }

                command = command
                    .replace("@name", mage.getName())
                    .replace("@world", location.getWorld().getName())
                    .replace("@x", Double.toString(location.getX()))
                    .replace("@y", Double.toString(location.getY()))
                    .replace("@z", Double.toString(location.getZ()));

                if (allDamagers) {
                    for (Entity damager : damagers) {
                        String damagerCommand = command.replace("@damagers", damager.getName());
                        mage.getController().getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), damagerCommand);
                    }
                } else {
                    mage.getController().getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }
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
