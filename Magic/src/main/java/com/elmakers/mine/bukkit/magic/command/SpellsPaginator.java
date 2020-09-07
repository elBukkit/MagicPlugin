package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

public class SpellsPaginator extends Paginator<SpellTemplate> {
    private final MageController controller;

    public SpellsPaginator(MageController controller) {
        this.controller = controller;
    }

    @Nonnull
    @Override
    protected List<SpellTemplate> getList(CommandSender sender) {
        Collection<SpellTemplate> spells = controller.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
        List<SpellTemplate> list = new ArrayList<>(spells);
        Collections.sort(list, new Comparator<SpellTemplate>() {
            @Override
            public int compare(SpellTemplate spell1, SpellTemplate spell2) {
                return spell1.getName().compareTo(spell2.getName());
            }
        });
        return list;
    }

    @Nonnull
    @Override
    protected String describe(SpellTemplate spell) {
        String name = spell.getName();
        String description = spell.getDescription();
        if (!name.equals(spell.getKey())) {
            description = name + " : " + description;
        }
        MaterialAndData spellIcon = spell.getIcon();
        Material material = spellIcon == null ? null : spellIcon.getMaterial();
        String icon = material == null ? "None" : material.name().toLowerCase();
        return ChatColor.AQUA + spell.getKey() + ChatColor.BLUE + " [" + icon + "] : " + ChatColor.YELLOW + description;
    }

    @Nonnull
    @Override
    protected String getTypeNamePlural() {
        return "spells";
    }
}
