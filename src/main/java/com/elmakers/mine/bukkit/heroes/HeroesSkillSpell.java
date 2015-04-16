package com.elmakers.mine.bukkit.heroes;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class HeroesSkillSpell extends BaseSpell {
    private String skillKey;

    @Override
    public void loadTemplate(String key, ConfigurationSection template) {
        super.loadTemplate(key, template);
        skillKey = key.substring(7);
        showUndoable = false;
    }

    @Override
    public SpellResult onCast(ConfigurationSection parameters) {
        CommandSender sender = mage.getCommandSender();
        controller.getPlugin().getServer().dispatchCommand(sender, "skill " + skillKey);
        return SpellResult.CAST;
    }
}
