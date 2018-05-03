package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

public class SpellsCommandExecutor extends MagicTabExecutor {

    public SpellsCommandExecutor(MagicAPI api) {
        super(api, "spells");
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String comandName, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }
        listSpells(sender, -1, args.length > 0 ? args[0] : null);
        return true;
    }


    public void listSpellsByCategory(CommandSender sender, String category)
    {
        List<SpellTemplate> categorySpells = new ArrayList<>();
        Collection<SpellTemplate> spellVariants = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
        Player player = sender instanceof Player ? (Player)sender : null;
        for (SpellTemplate spell : spellVariants)
        {
            if (spell.isHidden() || spell.getSpellKey().getLevel() > 1) continue;
            SpellCategory spellCategory = spell.getCategory();
            if (spellCategory != null && spellCategory.getKey().equalsIgnoreCase(category)
                && (player == null || spell.hasCastPermission(player)))
            {
                categorySpells.add(spell);
            }
        }

        if (categorySpells.size() == 0)
        {
            String message = api.getMessages().get("general.no_spells_in_category");
            message = message.replace("$category", category);
            sender.sendMessage(message);
            return;
        }
        sender.sendMessage(category + ":");
        Collections.sort(categorySpells);
        for (SpellTemplate spell : categorySpells)
        {
            String name = spell.getName();
            String description = spell.getDescription();
            if (!name.equals(spell.getKey())) {
                description = name + " : " + description;
            }
            sender.sendMessage(ChatColor.AQUA + spell.getKey() + ChatColor.BLUE + " [" + spell.getIcon().getName() + "] : " + ChatColor.YELLOW + description);
        }
    }

    public void listCategories(Player player)
    {
        HashMap<String, Integer> spellCounts = new HashMap<>();
        List<SpellCategory> spellGroups = new ArrayList<>();
        Collection<SpellTemplate> spellVariants = api.getSpellTemplates(player == null || player.hasPermission("Magic.bypass_hidden"));

        for (SpellTemplate spell : spellVariants)
        {
            if (player != null && !spell.hasCastPermission(player)) continue;
            if (spell.getCategory() == null) continue;

            Integer spellCount = spellCounts.get(spell.getCategory().getKey());
            if (spellCount == null || spellCount == 0)
            {
                spellCounts.put(spell.getCategory().getKey(), 1);
                spellGroups.add(spell.getCategory());
            }
            else
            {
                spellCounts.put(spell.getCategory().getKey(), spellCount + 1);
            }
        }
        if (spellGroups.size() == 0)
        {
            player.sendMessage(api.getMessages().get("general.no_spells"));
            return;
        }

        Collections.sort(spellGroups);
        for (SpellCategory group : spellGroups)
        {
            player.sendMessage(group.getName() + " [" + spellCounts.get(group.getKey()) + "]");
        }
    }

    public void listSpells(CommandSender sender, int pageNumber, String category)
    {
        if (category != null)
        {
            listSpellsByCategory(sender, category);
            return;
        }
        Player player = sender instanceof Player ? (Player)sender : null;
        Collection<SpellTemplate> spellVariants = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));

        int spellCount = 0;
        for (SpellTemplate spell : spellVariants)
        {
            if (player != null && !spell.hasCastPermission(player))
            {
                continue;
            }
            if (spell.getCategory() == null || spell.isHidden() || spell.getSpellKey().getLevel() > 1) continue;
            spellCount++;
        }

        // Kinda hacky internals-reaching
        Collection<SpellCategory> allCategories = api.getController().getCategories();
        List<SpellCategory> sortedGroups = new ArrayList<>(allCategories);
        Collections.sort(sortedGroups);

        int maxLines = -1;
        if (pageNumber >= 0) {
            maxLines = 5;
            int maxPages = spellCount / maxLines + 1;
            if (pageNumber > maxPages)
            {
                pageNumber = maxPages;
            }
            String message = api.getMessages().get("general.spell_list_page");
            message = message.replace("$count", Integer.toString(spellCount));
            message = message.replace("$pages", Integer.toString(maxPages));
            message = message.replace("$page", Integer.toString(pageNumber));
            sender.sendMessage(message);
        } else {
            String message = api.getMessages().get("general.spell_list");
            message = message.replace("$count", Integer.toString(spellCount));
            sender.sendMessage(message);
        }

        int currentPage = 1;
        int lineCount = 0;
        int printedCount = 0;
        for (SpellCategory group : sortedGroups)
        {
            if (printedCount > maxLines && maxLines > 0) break;

            boolean isFirst = true;
            Collection<SpellTemplate> spells = group.getSpells();
            for (SpellTemplate spell : spells)
            {
                if (printedCount > maxLines && maxLines > 0) break;
                if (!spell.hasCastPermission(sender) || spell.isHidden()) continue;

                if (currentPage == pageNumber || maxLines < 0)
                {
                    if (isFirst)
                    {
                        sender.sendMessage(group.getName() + ":");
                        isFirst = false;
                    }
                    String name = spell.getName();
                    String description = spell.getDescription();
                    if (!name.equals(spell.getKey())) {
                        description = name + " : " + description;
                    }
                    MaterialAndData spellIcon = spell.getIcon();
                    Material material = spellIcon == null ? null : spellIcon.getMaterial();
                    String icon = material == null ? "None" : material.name().toLowerCase();
                    sender.sendMessage(ChatColor.AQUA + spell.getKey() + ChatColor.BLUE + " [" + icon + "] : " + ChatColor.YELLOW + description);
                    printedCount++;
                }
                lineCount++;
                if (lineCount == maxLines)
                {
                    lineCount = 0;
                    currentPage++;
                }
            }
        }
    }
}
