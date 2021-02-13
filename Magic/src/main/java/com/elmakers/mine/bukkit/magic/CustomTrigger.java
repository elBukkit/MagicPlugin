package com.elmakers.mine.bukkit.magic;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Trigger;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class CustomTrigger extends Trigger {
    protected static class SpellCast {
        public final String spell;
        public final ConfigurationSection parameters;

        public SpellCast(String spell, ConfigurationSection parameters) {
            this.spell = spell;
            this.parameters = parameters;
        }

        public SpellCast(String spell) {
            this(spell, null);
        }
    }

    protected Deque<WeightedPair<SpellCast>> spells;
    protected Collection<EffectPlayer> effects;
    protected List<String> commands;

    public CustomTrigger(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        super(controller, configuration, key);

        if (configuration.isString("cast")) {
            String castCommand = configuration.getString("cast");
            if (!castCommand.isEmpty()) {
                spells = new ArrayDeque<>();
                String[] pieces = StringUtils.split(castCommand, " ");
                String spellKey = pieces[0];
                ConfigurationSection parameters = null;

                if (pieces.length > 1) {
                    String[] castParameters = Arrays.copyOfRange(pieces, 1, pieces.length);
                    parameters = ConfigurationUtils.newConfigurationSection();
                    ConfigurationUtils.addParameters(castParameters, parameters);
                }
                spells.add(new WeightedPair<>(new SpellCast(spellKey, parameters)));
            }
        } else if (configuration.isConfigurationSection("cast")) {
            ArrayDeque<WeightedPair<String>> spellKeys = new ArrayDeque<>();
            spells = new ArrayDeque<>();
            RandomUtils.populateStringProbabilityMap(spellKeys, configuration, "cast");
            for (WeightedPair<String> spellKey : spellKeys) {
                spells.add(new WeightedPair<>(spellKey, new SpellCast(spellKey.getValue())));
            }
        } else if (configuration.isList("cast")) {
            List<?> checkList = configuration.getList("cast");
            if (!checkList.isEmpty()) {
                Object first = checkList.get(0);
                if (first instanceof String) {
                    // How to really make this cast?
                    @SuppressWarnings("unchecked")
                    List<String> stringList = (List<String>)checkList;
                    spells = new ArrayDeque<>();
                    ArrayDeque<WeightedPair<String>> spellKeys = new ArrayDeque<>();
                    RandomUtils.populateStringProbabilityList(spellKeys, stringList);
                    for (WeightedPair<String> spellKey : spellKeys) {
                        spells.add(new WeightedPair<>(spellKey, new SpellCast(spellKey.getValue())));
                    }
                } else if (first instanceof ConfigurationSection || first instanceof Map) {
                    float currentThreshold = 0;
                    spells = new ArrayDeque<>();
                    for (Object configGeneric : checkList) {
                        ConfigurationSection config = null;
                        if (configGeneric instanceof ConfigurationSection) {
                            config = (ConfigurationSection)configGeneric;
                        }
                        if (configGeneric instanceof Map) {
                            // Arggggg
                            @SuppressWarnings("unchecked")
                            Map<String, ?> configMap = (Map<String, ?>)configGeneric;
                            config = ConfigurationUtils.toConfigurationSection(configuration, configMap);
                        }
                        if (config == null) continue;
                        String spellKey = config.getString("spell");
                        if (spellKey == null || spellKey.isEmpty()) {
                            controller.getLogger().warning("Trigger spell config missing 'spell' key");
                            continue;
                        }
                        config.set("spell", null);

                        currentThreshold += (float)config.getDouble("probability", 1);
                        spells.add(new WeightedPair<>(currentThreshold, new SpellCast(spellKey, config)));
                    }
                }
            }
        }
        commands = ConfigurationUtils.getStringList(configuration, "commands");
        if (configuration.contains("effects")) {
            effects = controller.loadEffects(configuration, "effects");
        }
    }

    private void cast(Mage mage, String castSpell) {
        cast(mage, castSpell, null);
    }

    private void cast(Mage mage, String castSpell, ConfigurationSection parameters) {
        if (castSpell.isEmpty() || castSpell.equalsIgnoreCase("none")) {
            return;
        }

        if (parameters != null) {
            parameters = ConfigurationUtils.cloneConfiguration(parameters);
        }
        Spell spell = null;
        if (castSpell.contains(" ")) {
            String[] additionalParameters = StringUtils.split(castSpell, ' ');
            castSpell = additionalParameters[0];
            additionalParameters = Arrays.copyOfRange(additionalParameters, 1, additionalParameters.length);
            if (parameters == null) {
                parameters = ConfigurationUtils.newConfigurationSection();
            }
            ConfigurationUtils.addParameters(additionalParameters, parameters);
        }
        spell = mage.getSpell(castSpell);
        if (spell == null) {
            mage.getController().getLogger().warning("Unknown spell in mob trigger: " + castSpell + " from mob " + mage.getName());
            return;
        }

        double bowpull = mage.getLastBowPull();
        if (bowpull > 0) {
            if (parameters == null) {
                parameters = ConfigurationUtils.newConfigurationSection();
            }
            parameters.set("bowpull", Double.toString(bowpull));
        }
        spell.cast(parameters);
    }

    public boolean cancel(Mage mage) {
        if (!isValid(mage)) {
            return false;
        }
        if (effects != null) {
            for (EffectPlayer player : effects) {
                player.cancel();
            }
        }

        if (spells != null && !spells.isEmpty()) {
            for (WeightedPair<SpellCast> cast : spells) {
                mage.cancelPending(cast.getValue().spell);
            }
        }
        return true;
    }

    public boolean execute(Mage mage) {
        if (!isValid(mage)) {
            return false;
        }
        triggered();

        if (effects != null) {
            for (EffectPlayer player : effects) {
                player.start(mage.getEffectContext());
            }
        }
        if (spells != null && !spells.isEmpty()) {
            SpellCast spellCast = RandomUtils.weightedRandom(spells);
            cast(mage, spellCast.spell, spellCast.parameters);
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

                boolean allDamagers = command.contains("@damagers");
                if (allDamagers && damagers == null) {
                    continue;
                }

                if (!allDamagers && command.contains("@damager")) {
                    if (topDamager == null) continue;
                    command = command.replace("@damager", topDamager.getName());
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

        return true;
    }
}
