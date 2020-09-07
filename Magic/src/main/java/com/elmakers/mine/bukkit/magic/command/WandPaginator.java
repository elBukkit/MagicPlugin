package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.WandTemplate;

public class WandPaginator extends Paginator<WandTemplate> {
    private final MageController controller;

    public WandPaginator(MageController controller) {
        this.controller = controller;
    }

    @Nonnull
    @Override
    protected List<WandTemplate> getList(CommandSender sender) {
        List<WandTemplate> templates = new ArrayList<>(controller.getWandTemplates());
        Collections.sort(templates, new Comparator<WandTemplate>() {
            @Override
            public int compare(WandTemplate wand1, WandTemplate wand2) {
                return wand1.getKey().compareTo(wand2.getKey());
            }
        });
        if (!sender.hasPermission("Magic.bypass_hidden")) {
            Iterator<WandTemplate> it = templates.iterator();
            while (it.hasNext()) {
                ConfigurationSection configuration = it.next().getConfiguration();
                if (configuration.getBoolean("hidden", false)) it.remove();;
            }
        }
        return templates;
    }

    @Nonnull
    @Override
    protected String describe(WandTemplate template) {
        String key = template.getKey();
        String name = controller.getMessages().get("wands." + key + ".name", controller.getMessages().get("wand.default_name"));
        String description = controller.getMessages().get("wands." + key + ".description", "");
        description = ChatColor.YELLOW + description;
        if (!name.equals(key)) {
            description = ChatColor.BLUE + name + ChatColor.WHITE + " : " + description;
        }
        return ChatColor.AQUA + key + ChatColor.WHITE + " : " + description;
    }

    @Nonnull
    @Override
    protected String getTypeNamePlural() {
        return "wands";
    }
}
