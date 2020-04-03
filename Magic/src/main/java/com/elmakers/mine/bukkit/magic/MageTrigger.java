package com.elmakers.mine.bukkit.magic;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class MageTrigger {
    public enum MageTriggerType {
        INTERVAL, DEATH, DAMAGE, SPAWN, LAUNCH
    }

    protected String type;
    protected Deque<WeightedPair<String>> spells;
    protected Collection<EffectPlayer> effects;
    protected List<String> commands;

    protected double maxHealth;
    protected double minHealth;
    protected double maxHealthPercentage;
    protected double minHealthPercentage;
    protected double maxDamage;
    protected double minDamage;

    public MageTrigger(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        String typeString = configuration.getString("type", key);
        try {
            String upperType = typeString.toUpperCase();
            MageTriggerType.valueOf(upperType);
            type = upperType;
        } catch (Exception ex) {
            type = typeString;
        }

        if (configuration.contains("cast")) {
            spells = new ArrayDeque<>();
            RandomUtils.populateStringProbabilityMap(spells, configuration, "cast");
        }
        commands = ConfigurationUtils.getStringList(configuration, "commands");

        maxHealth = configuration.getDouble("max_health");
        minHealth = configuration.getDouble("min_health");
        maxHealthPercentage = configuration.getDouble("max_health_percentage");
        minHealthPercentage = configuration.getDouble("min_health_percentage");
        maxDamage = configuration.getDouble("max_damage");
        minDamage = configuration.getDouble("min_damage");

        if (configuration.contains("effects")) {
            effects = controller.loadEffects(configuration, "effects");
        }
    }

    public String getType() {
        return type;
    }

    private void cast(Mage mage, String castSpell, double bowpull) {
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
            if (bowpull > 0) {
                if (parameters == null) {
                    parameters = new String[]{"bowpull", Double.toString(bowpull)};
                } else {
                    String[] existing = parameters;
                    parameters = new String[existing.length + 2];
                    System.arraycopy(existing, 0, parameters, 0, existing.length);
                    parameters[existing.length - 2] = "bowpull";
                    parameters[existing.length - 1] = Double.toString(bowpull);
                }
            }
            if (spell != null) {
                spell.cast(parameters);
            }
        }
    }

    public void execute(Mage mage) {
        execute(mage, 0, 0);
    }

    public void execute(Mage mage, double damage) {
        execute(mage, damage, 0);
    }

    public void execute(Mage mage, double damage, double bowpull) {
        if (minDamage > 0 && damage < minDamage) return;
        if (maxDamage > 0 && damage > maxDamage) return;

        LivingEntity li = mage.getLivingEntity();
        if (minHealth > 0 && (li == null || li.getHealth() < minHealth)) return;
        if (maxHealth > 0 && (li == null || li.getHealth() > maxHealth)) return;
        if (minHealthPercentage > 0 && (li == null || li.getHealth() * 100 / CompatibilityUtils.getMaxHealth(li) < minHealthPercentage)) return;
        if (maxHealthPercentage > 0 && (li == null || li.getHealth() * 100 / CompatibilityUtils.getMaxHealth(li) > maxHealthPercentage)) return;

        if (effects != null) {
            for (EffectPlayer player : effects) {
                player.start(mage.getEffectContext());
            }
        }
        if (spells != null && !spells.isEmpty()) {
            String spell = RandomUtils.weightedRandom(spells);
            cast(mage, spell, bowpull);
        }
        if (commands != null) {
            Entity topDamager = mage.getTopDamager();
            Entity killer = mage.getLastDamager();
            Collection<Entity> damagers = mage.getDamagers();
            Location location = mage.getLocation();
            for (String command : commands) {
                if (command.contains("@killer")) {
                    if (killer == null) continue;
                    command = command.replace("@killer", killer.getName());
                }
                if (command.contains("@damager")) {
                    if (topDamager == null) continue;
                    command = command.replace("@damager", topDamager.getName());
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
}
