package com.elmakers.mine.bukkit.magic.command;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.MagicConfigurable;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.BaseMagicConfigurable;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;

public abstract class MagicConfigurableExecutor extends MagicTabExecutor {
    public MagicConfigurableExecutor(MagicAPI api, String command) {
        super(api, command);
    }

    public MagicConfigurableExecutor(MagicAPI api, String[] commands) {
        super(api, commands);
    }

    public boolean onConfigure(String command, MagicConfigurable target, CommandSender sender, Player player, String[] parameters, boolean safe)
    {
        if (parameters.length < 1 || (safe && parameters.length < 2)) {
            sender.sendMessage("Use: /" + command + " configure <property> [value]");
            sender.sendMessage("Properties: " + StringUtils.join(BaseMagicConfigurable.PROPERTY_KEYS, ", "));
            return true;
        }

        Mage mage = controller.getMage(player);
        String value = "";
        for (int i = 1; i < parameters.length; i++) {
            if (i != 1) value = value + " ";
            value = value + parameters[i];
        }
        if (value.isEmpty()) {
            value = null;
        } else if (value.equals("\"\"")) {
            value = "";
        }
        if (value != null) {
            value = value.replace("\\n", "\n");
        }
        boolean modified = false;
        if (value == null) {
            if (target.removeProperty(parameters[0])) {
                modified = true;
                mage.sendMessage(api.getMessages().get(command + ".removed_property").replace("$name", parameters[0]));
            } else {
                mage.sendMessage(api.getMessages().get(command + ".no_property").replace("$name", parameters[0]));
            }
        } else {
            ConfigurationSection node = ConfigurationUtils.newConfigurationSection();

            double transformed = Double.NaN;
            try {
                transformed = Double.parseDouble(value);
            } catch (Exception ex) {
                EquationTransform transform = EquationStore.getInstance().getTransform(value);
                if (transform.getException() == null) {
                    double property = target.getProperty(parameters[0], Double.NaN);
                    if (!Double.isNaN(property)) {
                        transform.setVariable("x", property);
                        transformed = transform.get();
                    }
                }
            }

            if (!Double.isNaN(transformed)) {
                node.set(parameters[0], transformed);
            } else {
                node.set(parameters[0], value);
            }
            if (safe) {
                modified = target.upgrade(node);
            } else {
                target.configure(node);
                modified = true;
            }
            if (modified) {
                mage.sendMessage(api.getMessages().get(command + ".reconfigured"));
            } else {
                mage.sendMessage(api.getMessages().get(command + ".not_reconfigured"));
            }
        }
        if (sender != player) {
            if (modified) {
                sender.sendMessage(api.getMessages().getParameterized(command + ".player_reconfigured", "$name", player.getName()));
            } else {
                sender.sendMessage(api.getMessages().getParameterized(command + ".player_not_reconfigured", "$name", player.getName()));
            }
        }
        return true;
    }

    protected boolean onLevelSpells(String command, CommandSender sender, Player player, CasterProperties caster, Integer maxLevel) {
        Collection<String> spells = caster.getSpells();
        MageController controller = api.getController();
        int levelledCount = 0;
        for (String spellKey : spells) {
            SpellTemplate spellTemplate = controller.getSpellTemplate(spellKey);
            if (spellTemplate == null) continue;
            SpellKey key = spellTemplate.getSpellKey();
            int currentLevel = key.getLevel();
            if (maxLevel != null && currentLevel >= maxLevel) continue;

            int targetLevel = key.getLevel();
            while (spellTemplate != null && (maxLevel == null || targetLevel < maxLevel)) {
                key = new SpellKey(key.getBaseKey(), targetLevel + 1);
                spellTemplate = controller.getSpellTemplate(key.getKey());
                if (spellTemplate != null) {
                    targetLevel++;
                }
            }
            if (currentLevel >= targetLevel) continue;
            key = new SpellKey(key.getBaseKey(), targetLevel);
            caster.addSpell(key.getKey());

            levelledCount++;
        }

        if (sender != player) {
            if (levelledCount > 0) {
                sender.sendMessage(api.getMessages().getParameterized(command + ".player_spells_levelled", "$name", player.getName(), "$count", Integer.toString(levelledCount)));
            } else {
                sender.sendMessage(api.getMessages().getParameterized(command + ".player_spells_not_levelled", "$name", player.getName()));
            }
        } else {
            if (levelledCount > 0) {
                sender.sendMessage(api.getMessages().getParameterized(command + ".spells_levelled", "$name", player.getName(), "$count", Integer.toString(levelledCount)));
            } else {
                sender.sendMessage(api.getMessages().getParameterized(command + ".spells_not_levelled", "$name", player.getName()));
            }
        }

        return true;
    }
}
