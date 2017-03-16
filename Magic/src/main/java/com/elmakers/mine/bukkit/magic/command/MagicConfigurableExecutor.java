package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.MagicConfigurable;
import com.elmakers.mine.bukkit.magic.BaseMagicConfigurable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;

public abstract class MagicConfigurableExecutor extends MagicTabExecutor {
    public MagicConfigurableExecutor(MagicAPI api) {
        super(api);
    }

    public boolean onConfigure(String command, MagicConfigurable target, CommandSender sender, Player player, String[] parameters, boolean safe)
    {
        if (parameters.length < 1 || (safe && parameters.length < 2)) {
            sender.sendMessage("Use: /" + command + " configure <property> [value]");
            sender.sendMessage("Properties: " + StringUtils.join(BaseMagicConfigurable.PROPERTY_KEYS, ", "));
            return true;
        }

        Mage mage = api.getMage(player);
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
        boolean modified = false;
        if (value == null) {
            if (target.removeProperty(parameters[0])) {
                modified = true;
                mage.sendMessage(api.getMessages().get(command + ".removed_property").replace("$name", parameters[0]));
            } else {
                mage.sendMessage(api.getMessages().get(command + ".no_property").replace("$name", parameters[0]));
            }
        } else {
            ConfigurationSection node = new MemoryConfiguration();
            node.set(parameters[0], value);
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
        mage.checkWand();
        if (sender != player) {
            if (modified) {
                sender.sendMessage(api.getMessages().getParameterized(command + ".player_reconfigured", "$name", player.getName()));
            } else {
                sender.sendMessage(api.getMessages().getParameterized(command + ".player_not_reconfigured", "$name", player.getName()));
            }
        }
        return true;
    }
}
