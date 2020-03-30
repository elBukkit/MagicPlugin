package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.citizens.CitizensController;
import com.elmakers.mine.bukkit.citizens.CitizensTrait;
import com.elmakers.mine.bukkit.citizens.CommandCitizensTrait;
import com.elmakers.mine.bukkit.citizens.MagicCitizensTrait;
import com.elmakers.mine.bukkit.spell.BaseSpell;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class MagicTraitCommandExecutor extends MagicTabExecutor {
    protected final CitizensController citizensController;

    public MagicTraitCommandExecutor(MagicAPI api, CitizensController citizensController) {
        super(api, "mtrait");
        this.citizensController = citizensController;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode()))
        {
            sendNoPermission(sender);
            return true;
        }

        NPC npc = null;
        Citizens citizens = citizensController.getCitizensPlugin();
        // Did player specify a id?
        if (args.length > 0) {

            try {
                int npcId = Integer.parseInt(args[0]);
                npc = CitizensAPI.getNPCRegistry().getById(npcId);
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            catch (Exception ignore) {
            }
        }

        if (npc == null)
        {
            npc = citizens.getNPCSelector().getSelected(sender);
        }

        if (npc == null)
        {
            sender.sendMessage(ChatColor.RED + "Usage: mtrait <id#> <property> <value>");
            return true;
        }

        CitizensTrait trait = null;
        if (npc.hasTrait(MagicCitizensTrait.class))
        {
            trait = npc.getTrait(MagicCitizensTrait.class);
        }
        else if (npc.hasTrait(CommandCitizensTrait.class))
        {
            trait = npc.getTrait(CommandCitizensTrait.class);
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "You must add a \"magic\" or \"command\" trait first");
            return true;
        }

        if (args.length == 0)
        {
            trait.describe(sender);
        }
        else
        {
            String key = args[0];
            String value = args.length > 1 ? args[1] : null;
            for (int i = 2; i < args.length; i++) {
                value = value + " " + args[i];
            }
            trait.configure(sender, key, value);
        }

        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (!sender.hasPermission("Magic.commands.mtrait")) return options;

        String lastParameter = "";
        if (args.length > 1) {
            lastParameter = args[args.length - 2];
        }

        if (lastParameter.equalsIgnoreCase("spell"))
        {
            Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
            for (SpellTemplate spell : spellList) {
                addIfPermissible(sender, options, "Magic.cast.", spell.getKey());
            }
        }
        else if (lastParameter.equalsIgnoreCase("parameters"))
        {
            options.addAll(Arrays.asList(BaseSpell.COMMON_PARAMETERS));
        }
        else if (lastParameter.equalsIgnoreCase("hat") || lastParameter.equalsIgnoreCase("requires"))
        {
            Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
            for (SpellTemplate spell : spellList) {
                options.add(spell.getKey());
            }
            Collection<String> allWands = api.getWandKeys();
            for (String wandKey : allWands) {
                options.add(wandKey);
            }
            for (Material material : Material.values()) {
                options.add(material.name().toLowerCase());
            }
            Collection<String> allItems = api.getController().getItemKeys();
            for (String itemKey : allItems) {
                options.add(itemKey);
            }
        }
        else if (lastParameter.equalsIgnoreCase("mob"))
        {
            options.addAll(controller.getMobKeys());
        }
        else if (lastParameter.equalsIgnoreCase("cost"))
        {
            options.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
        else if (
                lastParameter.equalsIgnoreCase("caster")
                || lastParameter.equalsIgnoreCase("invisible")
                || lastParameter.equalsIgnoreCase("target_player")
                || lastParameter.equalsIgnoreCase("message_player")) {
            options.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        }
        else
        {
            options.add("spell");
            options.add("parameter");
            options.add("caster");
            options.add("target_player");
            options.add("message_player");
            options.add("cost");
            options.add("permission");
            options.add("invisible");
            options.add("hat");
            options.add("command");
            options.add("mob");
            options.add("requires");

            Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
            for (SpellTemplate spell : spellList) {
                addIfPermissible(sender, options, "Magic.cast.", spell.getKey());
            }
        }

        return options;
    }
}
