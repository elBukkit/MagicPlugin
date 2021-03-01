package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;

public class CheckLoreAction extends CheckAction {
    private Pattern pattern;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        this.pattern = Pattern.compile(ChatColor.translateAlternateColorCodes('&', parameters.getString("pattern")));
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        Entity entity = context.getTargetEntity();
        if (!(entity instanceof Player))
            return false;
        Player player = (Player)entity;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null)
            return false;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta == null ? null : meta.getLore();
        if (lore == null)
            return false;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("pattern");
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
